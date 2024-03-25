package Peer.Messages;

import java.util.BitSet;

public class Bitfield {
    private BitSet bitfield; // Represents the bitfield indicating which pieces the peer has

    // Constructor
    public Bitfield(BitSet bitfield) {
        this.bitfield = bitfield;
    }

    // Getter for bitfield
    public BitSet getBitfield() {
        return bitfield;
    }

    // Setter for bitfield
    public void setBitfield(BitSet bitfield) {
        this.bitfield = bitfield;
    }

    // Additional methods or logic related to the Bitfield message
}
