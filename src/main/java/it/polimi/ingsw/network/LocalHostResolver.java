package it.polimi.ingsw.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resolves the IPv4 address to advertise as {@code java.rmi.server.hostname}
 * so that remote peers can open callback connections back to this JVM.
 * <p>
 * It deliberately avoids {@link InetAddress#getLocalHost()}: on Debian/Ubuntu
 * the host name is mapped to the loopback alias {@code 127.0.1.1} in
 * {@code /etc/hosts}, which makes RMI export stubs pointing at an address the
 * remote peer cannot reach. Both the server ({@code App}) and the RMI client
 * share this resolver so the two ends behave identically across operating
 * systems.
 */
public final class LocalHostResolver {

    /** System property RMI reads to build the address embedded in exported stubs. */
    public static final String RMI_HOSTNAME_PROPERTY = "java.rmi.server.hostname";

    /** Raw address length, in bytes, that identifies an IPv4 address. */
    private static final int IPV4_BYTE_COUNT = 4;

    private LocalHostResolver() {
    }

    /**
     * Returns the address to publish via RMI.
     * <p>
     * An explicit {@code -D}{@value #RMI_HOSTNAME_PROPERTY} override always wins;
     * otherwise the first site-local IPv4 address found on an active,
     * non-loopback interface is used. When several candidates exist the first is
     * chosen and the alternatives are logged so a wrong pick can be overridden.
     *
     * @return the host address to advertise via RMI
     * @throws HostResolutionException if no suitable address can be found or the
     *                                 network interfaces cannot be enumerated
     */
    public static String resolve() {
        String override = System.getProperty(RMI_HOSTNAME_PROPERTY);
        if (override != null && !override.isBlank()) {
            return override;
        }

        List<String> candidates = siteLocalIPv4Addresses();
        if (candidates.isEmpty()) {
            throw new HostResolutionException(
                    "No active non-loopback IPv4 interface found; pass the address "
                    + "explicitly with -D" + RMI_HOSTNAME_PROPERTY);
        }
        if (candidates.size() > 1) {
            System.err.println("[Net] Multiple local addresses found " + candidates
                    + " - using " + candidates.get(0) + "; override with -D"
                    + RMI_HOSTNAME_PROPERTY + " if it is the wrong one");
        }
        return candidates.get(0);
    }

    private static List<String> siteLocalIPv4Addresses() {
        List<String> result = new ArrayList<>();
        try {
            for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!isUsable(nif)) {
                    continue;
                }
                for (InetAddress address : Collections.list(nif.getInetAddresses())) {
                    if (isSiteLocalIPv4(address)) {
                        result.add(address.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            throw new HostResolutionException("Cannot enumerate network interfaces", e);
        }
        return result;
    }

    private static boolean isUsable(NetworkInterface nif) throws SocketException {
        return nif.isUp() && !nif.isLoopback() && !nif.isVirtual() && !nif.isPointToPoint();
    }

    private static boolean isSiteLocalIPv4(InetAddress address) {
        return address.getAddress().length == IPV4_BYTE_COUNT && address.isSiteLocalAddress();
    }
}
