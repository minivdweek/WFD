package packet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;

import static java.lang.System.arraycopy;

/**
 * The internal Packet Structure, to create and retrieve packets on the network
 * Created by joris.vandijk on 05/04/16.
 */
public class Packet {

    //fields in the header
    private int dst;
    private int src;
    private int seqNo;
    private int ackNo;
    private int type;
    private int flags;
    private int windowSize;
    private int headerLength;
    private int dataLength;
    private int fileID;

    //header
    private byte[] header;
    //data
    private byte[] data;

    //construct new packet from raw data
    public Packet(int type, byte[] data) {
        this.type = type;
        this.data = data;
        this.dataLength = data.length;
    }

    //construct packet from an incoming DatagramPacket
    //!!!!!!!!can also check if the received packet is intact!!!!!!!
    public Packet(DatagramPacket datagramPacket) {
        byte[] packet = datagramPacket.getData();
        System.out.println(new String(packet));
        byte[] packetWOChecksum = new byte[packet.length - 1 ];
        arraycopy(packet, 0, packetWOChecksum, 0, packetWOChecksum.length);

        //check if the checksums match
        if (!createCheckSum(packetWOChecksum).equals(Arrays.copyOfRange(packet, packet.length - 3, packet.length))) {
            //TODO figure out what to do if checksums dont match
        }

        getHeaderLength(packet);
        header = new byte[headerLength];
        arraycopy(packet, 0, header, 0, headerLength);
        readHeader();
        data = new byte[packet.length-(header.length + 3)];
        arraycopy(packet, headerLength, data, 0, data.length);
        dataLength = data.length;
    }

    //return the byte array of this packet for sending, and append the checksum
    public byte[] toBytes() {
        setHeader();
        byte[] noCheckSum = concatenateHeaderData(header, data);
        byte[] packet = new byte[noCheckSum.length + 4];
        arraycopy(noCheckSum, 0, packet, 0, noCheckSum.length);
        byte[] cs = createCheckSum(noCheckSum);
        arraycopy(cs, 0, packet, noCheckSum.length, cs.length);
        return packet;
    }

    public DatagramPacket toDatagramPacket() {
        byte[] packet = toBytes();
        DatagramPacket result = new DatagramPacket(packet, packet.length, dstToIP(), 30000);
        return result;
    }

    public byte[] buildHeader(){
        byte[] hdr = new byte[12];
        hdr[0] = (byte) dst;
        hdr[1] = (byte) src;
        hdr[2] = (byte) (seqNo >> 8);
        hdr[3] = (byte) seqNo;
        hdr[4] = (byte) (ackNo >> 8);
        hdr[5] = (byte) ackNo;
        hdr[6] = (byte) (type << 4 + flags);
        hdr[7] = (byte) windowSize;
        hdr[8] = (byte) hdr.length;
        hdr[9] = (byte) dataLength;
        hdr[10] = (byte) fileID;
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
        seqNo = header[2] << 8 + header[3];
        ackNo = header[4] << 8 + header[5];
        type = header[6] >> 4;
        flags = header[6] ^ type;
        windowSize = header[7];
    }

    public byte[] createCheckSum(byte[] bytes){
        CRC32 crc = new CRC32();
        crc.update(bytes);
        //TODO fix crc, now returns 1 byte (?), should be 4
        Long CRC = crc.getValue();
        byte[] result = new byte[4];
        for (int b = 1; b <= result.length; b++) {
            result[b] = (byte) (CRC >> ((result.length - b) * 8));
        }
        return result;
    }

    private InetAddress dstToIP() {
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName("172.17.2." + dst);
        } catch (UnknownHostException e) {
            System.out.println("Error resolving host in packet");
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
        byte[] result = new byte[header.length + data.length];
        for (int r = 0; r < result.length; r++) {
            if (r < header.length) {
                result[r] = header[r];
            } else {
                result[r] = data[r - header.length];
            }
        }
        return result;
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

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setFileID(int fileID) {
        this.fileID = fileID;
    }

    public int getFileID() {
        return fileID;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
