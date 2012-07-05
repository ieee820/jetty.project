package org.eclipse.jetty.websocket.generator;

import java.nio.ByteBuffer;
import java.util.EnumMap;

import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.frames.BaseFrame;
import org.eclipse.jetty.websocket.protocol.OpCode;

/**
 * Generating a frame in WebSocket land.
 * 
 * <pre>
 *    0                   1                   2                   3
 *    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *   +-+-+-+-+-------+-+-------------+-------------------------------+
 *   |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
 *   |I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
 *   |N|V|V|V|       |S|             |   (if payload len==126/127)   |
 *   | |1|2|3|       |K|             |                               |
 *   +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
 *   |     Extended payload length continued, if payload len == 127  |
 *   + - - - - - - - - - - - - - - - +-------------------------------+
 *   |                               |Masking-key, if MASK set to 1  |
 *   +-------------------------------+-------------------------------+
 *   | Masking-key (continued)       |          Payload Data         |
 *   +-------------------------------- - - - - - - - - - - - - - - - +
 *   :                     Payload Data continued ...                :
 *   + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
 *   |                     Payload Data continued ...                |
 *   +---------------------------------------------------------------+
 * </pre>
 */
public class Generator
{
    private static final Logger LOG = Log.getLogger(Generator.class);

    private final EnumMap<OpCode, FrameGenerator<?>> generators = new EnumMap<>(OpCode.class);

    public Generator(WebSocketPolicy policy)
    {
        generators.put(OpCode.BINARY,new BinaryFrameGenerator(policy));
        generators.put(OpCode.TEXT,new TextFrameGenerator(policy));
        generators.put(OpCode.PING,new PingFrameGenerator(policy));
        generators.put(OpCode.PONG,new PongFrameGenerator(policy));
        generators.put(OpCode.CLOSE,new CloseFrameGenerator(policy));
    }

    @SuppressWarnings(
            { "unchecked", "rawtypes" })
    public ByteBuffer generate(ByteBuffer buffer, BaseFrame frame)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Buffer: {}",BufferUtil.toDetailString(buffer));
        }
        FrameGenerator generator = generators.get(frame.getOpCode());
        LOG.debug(generator.getClass().getSimpleName() + " active");
        return generator.generate(buffer,frame);
    }

    @Override
    public String toString()
    {
        return String.format("Generator {%s registered}",generators.size());
    }

}