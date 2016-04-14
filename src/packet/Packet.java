package packet;

import network.exceptions.BrokenPacketException;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.zip.CRC32;

import static java.lang.System.arraycopy;

/**
 * The internal Packet Structure, to create and retrieve packets on the network
 * Created by joris.vandijk on 05/04/16.
 */
public class Packet {
    //fields in the udp header
    private int portNo;
    private InetAddress source;

    //fields in the header
    private int dst;
    private int src;
    private int seqNo;
    private int ackNo;
    private int type;
    private int flags; //syn, ack, fin
    private int windowSize;
    private int headerLength;
    private int dataLength;
    //header
    private byte[] header;
    //data
    private byte[] data;

    //construct new packet from raw data
    public Packet(int type, byte[] data) {
        this.type = type;
        this.data = data;
        if (data!=null) {
            this.dataLength = data.length;
        }
        this.portNo = 30000;
    }

    //construct packet from an incoming DatagramPacket
    public Packet(DatagramPacket datagramPacket) throws BrokenPacketException {
        this.source = datagramPacket.getAddress();
        this.portNo = datagramPacket.getPort();
        byte[] packet = datagramPacket.getData();
        byte[] packetWOChecksum = Arrays.copyOf(packet, packet.length - 4);

        //get the info from the packet
        getHeaderLength(packet);
        header = new byte[headerLength];
        arraycopy(packet, 0, header, 0, headerLength);
        readHeader();
        data = new byte[dataLength];
        arraycopy(packet, headerLength, data, 0, dataLength);

        //check if the checksums match
        packetWOChecksum = new byte[headerLength+dataLength];
        arraycopy(header, 0, packetWOChecksum, 0, headerLength -1 );
        arraycopy(data, 0, packetWOChecksum, headerLength, dataLength);
        if (!Arrays.equals(createCheckSum(packetWOChecksum), Arrays.copyOfRange(packet, packetWOChecksum.length , packetWOChecksum.length + 4))) {
            throw new BrokenPacketException("The CheckSum does not match");
        }

    }

    //return the byte array of this packet for sending, and append the checksum
    public byte[] toBytes() {
        setHeader();
        byte[] noCheckSum = concatenateHeaderData(header, data);
        byte[] packet = Arrays.copyOf(noCheckSum, noCheckSum.length + 4);
        byte[] cs = createCheckSum(noCheckSum);
        arraycopy(cs, 0, packet, noCheckSum.length, cs.length);
        return packet;
    }

    public DatagramPacket toDatagramPacket() {
        byte[] packet = toBytes();
        return new DatagramPacket(packet, packet.length, dstToIP(), portNo);
    }

    public byte[] buildHeader(){
        byte[] hdr = new byte[12];
        hdr[0] = (byte) dst;
        hdr[1] = (byte) src;
        hdr[2] = (byte) (seqNo >> 8);
        hdr[3] = (byte) seqNo;
        hdr[4] = (byte) (ackNo >> 8);
        hdr[5] = (byte) ackNo;
        hdr[6] = (byte) ((type << 4) + flags);
        hdr[7] = (byte) windowSize;
        hdr[8] = (byte) hdr.length;
        hdr[9] = (byte) (dataLength >> 8);
        hdr[10] = (byte) dataLength;
        return hdr;
    }

    public void setHeader() {
        this.header = buildHeader();
    }

    public void getHeaderLength(byte[] packet) {
        this.headerLength = packet[8];
    }

    public void readHeader(){
        dst = header[0];
        src = header[1];
        seqNo = (header[2] << 8) + header[3];
        ackNo = (header[4] << 8) + header[5];
        type = header[6] >> 4;
        flags = header[6] ^ (type << 4);
        windowSize = header[7];
        dataLength = (header[9] << 8) + header [10];
    }

    public byte[] createCheckSum(byte[] bytes){
        CRC32 crc = new CRC32();
        crc.update(bytes);
        Long CRClong = crc.getValue();
        byte[] result = new byte[4];
        for (int b = 1; b <= result.length; b++) {
            result[b-1] = (byte) (CRClong >> ((result.length - b) * 8));
        }
        return result;
    }

    private InetAddress dstToIP() {
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName("172.17.2." + dst);
        } catch (UnknownHostException e) {
            System.out.println("Error resolving host for packet");
            e.printStackTrace();
        }
        return ip;
    }

    /**
     * concatenates the Header and Data parts of this Packet
     * @param header    the header array
     * @param data      the data array
     * @return concatenated array
     */
    private byte[] concatenateHeaderData(byte[] header, byte[] data) {
        if (data!=null) {
            byte[] result = new byte[header.length + data.length];
            for (int r = 0; r < result.length; r++) {
                if (r < header.length) {
                    result[r] = header[r];
                } else {
                    result[r] = data[r - header.length];
                }
            }
            return result;
        } else {
            return header;
        }
    }

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public int getAckNo() {
        return ackNo;
    }

    public void setAckNo(int ackNo) {
        this.ackNo = ackNo;
    }

    public int getSrc() {
        return src;
    }

    public void setSrc(int src) {
        this.src = (byte) src;
    }

    public int getDst() {
        return dst;
    }

    public void setDst(int dst) {
        this.dst = (byte) dst;
    }

    public byte[] getData() {
        return data;
    }

    public int getType() {
        return type;
    }

    public void setPortNo(int portNo) {
        this.portNo = portNo;
    }

    public int getPortNo() {
        return portNo;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getFlags() {
        return flags;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public int getWindowSize() {
        return windowSize;
    }
}
