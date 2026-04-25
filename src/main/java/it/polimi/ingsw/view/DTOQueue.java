package it.polimi.ingsw.view;

import it.polimi.ingsw.network.dto.DTO;
import it.polimi.ingsw.network.dto.DTOVisitor;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Single FIFO queue that decouples network I/O from UI rendering.
 *
 * Architecture
 * ────────────
 * Network threads (SocketClient, RMIclient) call push() to enqueue DTOs.
 * A dedicated consumer thread (started by ClientManager) calls run(), which
 * blocks on queue.take() and dispatches each DTO to the current visitor.
 *
 * Because push() only calls LinkedBlockingQueue.add() — which is thread-safe
 * by contract — no additional synchronization is needed on the producer side.
 * The consumer never needs to hold a lock while calling UI code.
 *
 * Visitor swap
 * ────────────
 * The active visitor is stored as a volatile field so that a write from one
 * thread (ClientManager calling setVisitor when STARTING_GAME arrives) is
 * immediately visible to the consumer thread without synchronization.
 * At worst, the last lobby message is processed by LobbyDTOHandler as a no-op
 * — there is no dangerous race condition.
 */
public class DTOQueue implements Runnable {

    // LinkedBlockingQueue is thread-safe: producers and the consumer never
    // need to synchronize on the queue object itself.
    private final LinkedBlockingQueue<DTO> queue = new LinkedBlockingQueue<>();

    // volatile guarantees that a write from any thread is immediately visible
    // to the consumer thread reading this field in run().
    private volatile DTOVisitor visitor;

    private volatile boolean going = true;

    public DTOQueue(DTOVisitor initialVisitor) {
        this.visitor = initialVisitor;
    }

    /** Called by network threads to enqueue an incoming DTO. */
    public void push(DTO dto) {
        queue.add(dto);
    }

    /**
     * Swaps the active visitor. Called once by ClientManager when the server
     * signals that the game is starting (LobbyState.STARTING_GAME), replacing
     * LobbyDTOHandler with GameDTOHandler for all subsequent messages.
     */
    public void setVisitor(DTOVisitor visitor) {
        this.visitor = visitor;
    }

    public void stop() {
        going = false;
    }

    /**
     * Consumer loop. Blocks on take() — no busy-waiting — and dispatches each
     * DTO via the Visitor pattern (dto.accept calls back visitor.visit(this)).
     */
    @Override
    public void run() {
        while (going) {
            try {
                DTO dto = queue.take(); // blocks until an element is available
                dto.accept(visitor);   // double dispatch: routes to the right visit() overload
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
