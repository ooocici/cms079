/*
 * MIT License
 *
 * Copyright (c) 2018 msemu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.msemu.commons.network;

import com.msemu.commons.enums.ClientState;
import com.msemu.commons.network.handler.AbstractPacketHandlerFactory;
import com.msemu.commons.network.packets.OutPacket;
import com.msemu.commons.utils.Rand;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Client<TClient extends Client<TClient>> {

    /**
     * Attribute key for this Client object.
     */
    public static final AttributeKey<Client> CLIENT_KEY = AttributeKey.valueOf("C");
    private static final Logger log = LoggerFactory.getLogger("Network");
    /**
     * ChannelInfo object associated with this specific client. Used for all
     * I/O response regarding a MapleStory game session.
     */
    @Getter
    protected final Connection<TClient> connection;
    /**
     * Lock regarding the encoding of packets to be sent to remote
     * sessions.
     */
    @Getter
    private final ReentrantLock lock;
    /**
     * Empty constructor for child class implementation.
     */
    private final AtomicReference<ClientState> state;
    /**
     * Send seed or IV for one of the cryptography stages.
     */
    @Getter
    @Setter
    private byte[] siv;
    /**
     * Receive seed or IV for one of the cryptography stages.
     */
    @Getter
    @Setter
    private byte[] riv;
    /**
     * Stored length used for packet decryption. This is used for
     * storing the packet length for the next packet that is readable.
     * Since TCP sessions ensure that all data arrives to the server in order,
     * we can decodeByte packet data in the correct order.
     */
    @Getter
    @Setter
    private int storedLength = -1;

    /**
     * Construct a new Client with the corresponding ChannelInfo that
     * will be used to write to as well as the send and recv seeds or IVs.
     *
     * @param connection the channel object associated with this client session.
     */
    public Client(Connection<TClient> connection) {
        this.connection = connection;
        this.siv = new byte[4];
        this.riv = new byte[4];
        Rand.nextBytes(this.siv);
        Rand.nextBytes(this.riv);
        lock = new ReentrantLock(true); // note: lock is fair to ensure logical sequence is maintained server-side
        this.state = new AtomicReference<>(ClientState.NULL);
    }

    /**
     * Writes a packet message to the channel. Gets encoded later in the
     * pipeline.
     *
     * @param msg the packet message to be sent.
     */
    public void write(OutPacket<TClient> msg) {
        connection.write(msg);
    }

    /**
     * Closes this channel and session.
     */
    public void close() {
        connection.close();
    }

    /**
     * Gets the remote IP address for this session.
     *
     * @return the remote IP address.
     */
    public String getIP() {
        if (getConnection().getSocketAddress() == null)
            return "0.0.0.0";
        return getConnection().getSocketAddress().toString().split(":")[0].substring(1);
    }

    /**
     * Acquires the encoding state for this specific send IV. This is to
     * prevent multiple encoding states to be possible at the same time. If
     * allowed, the send IV would mutate to an unusable IV and the session would
     * be dropped as a result.
     */
    public final void acquireEncoderState() {
        lock.lock();
    }

    /**
     * Releases the encoding state for this specific send IV.
     */
    public final void releaseEncodeState() {
        lock.unlock();
    }

    protected void onOpen() {
        if (this.state.compareAndSet(ClientState.NULL, ClientState.CONNECTED)) {
            log.info("Client [{}] connected.", this);
        }

    }

    protected void onClose() {
        if (this.state.compareAndSet(ClientState.CONNECTED, ClientState.DISCONNECTED)
                || this.state.compareAndSet(ClientState.AUTHED_GG, ClientState.DISCONNECTED)
                || this.state.compareAndSet(ClientState.AUTHED, ClientState.DISCONNECTED)
                || this.state.compareAndSet(ClientState.ENTERED, ClientState.DISCONNECTED)) {
            log.info("Client [{}] disconnected.", this);
        }

    }

    protected void onIdle() {
        log.info("Client [{}] Idle.", this);
    }

    public void closeForce() {
        getConnection().close();
    }

    public SocketAddress getSocketAddress() {
        return getConnection().getSocketAddress();
    }

    public boolean compareAndSetState(ClientState oldState, ClientState newState) {
        return this.state.compareAndSet(oldState, newState);
    }

    public ClientState getState() {
        return this.state.get();
    }

    public String getHostAddress() {
        return this.isConnected() ? this.getConnection().getSocketAddress().getAddress().getHostAddress() : "not connected";
    }

    public AbstractPacketHandlerFactory<TClient> getPacketHandler() {
        return this.connection.getServer().getPacketHandler();
    }

    public boolean isConnected() {
        return !this.getConnection().isClosedOrPending();
    }
}