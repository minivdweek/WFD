package packet;

import java.net.DatagramPacket;
import java.util.zip.CRC32;

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

        //check if the checksums match
        if (packet[packet.length - 1] != createCheckSum(concatenateHeaderData(header, data))) {
            //Throw a new exception, or ask for a resend
            //
        }

        getHeaderLength(packet);
        header = new byte[headerLength];
        for (int h = 0; h < headerLength; h++) {
            header[h] = packet[h];
        }
        readHeader();
        data = new byte[packet.length-(header.length + 1)];
        for (int d = 0; d < data.length; d++) {
            data[d] = packet[d+header.length];
        }
        dataLength = data.length;
    }

    //return the byte array of this packet for sending, and append the checksum
    public byte[] toBytes() {
        buildHeader();
        byte[] noCheckSum = concatenateHeaderData(header, data);
        byte[] packet = new byte[noCheckSum.length+1];
        for (int p = 0; p < noCheckSum.length; p++) {
            packet[p] = noCheckSum[p];
        }
        packet[noCheckSum.length] = createCheckSum(noCheckSum);
        return packet;
    }

    public DatagramPacket toDatagramPacket() {
        //DatagramPacket result = new DatagramPacket(toBytes(), toBytes().length, )
        return null;
    }

    public byte[] buildHeader(){
        byte[] hdr = new byte[10];
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

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte createCheckSum(byte[] bytes){
        CRC32 crc = new CRC32();
        crc.update(bytes);
        return (byte) crc.getValue();
    }

    /**
     * concatenates the Header and Data parts of this Packet
     * @param header
     * @param data
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

    public void setSeqNo(byte seqNo) {
        this.seqNo = seqNo;
    }

    public int getAckNo() {
        return ackNo;
    }

    public void setAckNo(byte ackNo) {
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
}
