package Peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class FileManager {
    private byte[][] pieces;
    public boolean[] hasPiece;
    private File file;
    private int pieceSize;

    public FileManager(String filePath, int pieceSize) throws IOException {
        this.pieceSize = pieceSize;
        this.file = new File(filePath);
        System.out.println(this.pieceSize);
        if (file.exists() && file.isFile()) {
            loadFile();
            System.out.println("This peer has read in the file.");
        } else {
            System.out.println("File does not exist, setting up for download.");
            this.pieces = new byte[calculateNumPieces(file.length())][];
            this.hasPiece = new boolean[calculateNumPieces(file.length())];
        }
    }

    private void loadFile() throws IOException {
        FileInputStream fis = new FileInputStream(file);
        int numPieces = calculateNumPieces(file.length());
        pieces = new byte[numPieces][];
        hasPiece = new boolean[numPieces];

        for (int i = 0; i < numPieces; i++) {
            pieces[i] = new byte[pieceSize];
            int bytesRead = fis.read(pieces[i], 0, pieceSize);
            if (bytesRead < pieceSize) {
                pieces[i] = java.util.Arrays.copyOf(pieces[i], bytesRead); // last filePiece might be < pieceSize...
            }
            hasPiece[i] = true;
        }
        fis.close();
    }

    public synchronized boolean hasPiece(int index) {
        if (index < 0 || index >= hasPiece.length) {
            return false;  // out of bounds index - can't own it
        }
        return hasPiece[index];
    }

    private int calculateNumPieces(long fileSize) {
        return (int) Math.ceil(fileSize / (double) pieceSize);
    }

    public synchronized byte[] getPiece(int index) {
        if (index < 0 || index >= pieces.length || !hasPiece[index]) {
            return null;
        }
        return pieces[index];
    }

    public synchronized void storePiece(int index, byte[] data) {
//        System.out.println("Attempting to store piece at " + index);
        System.out.println(index + " " + pieces.length + " " + data.length + " " + this.pieceSize + " " + data);
        if (index >= 0 && index < pieces.length && data != null && data.length <= pieceSize) {
            pieces[index] = data;
            hasPiece[index] = true;
//            System.out.println("Piece at " + index + " has been updated to " + hasPiece(index));
        }
    }

    public boolean[] getBitfield() {
        return hasPiece;
    }

    public boolean hasAllPieces() {
        for (boolean b : hasPiece) {
            if (!b) return false;
        }
        return true;
    }

    // constructor for when a peer does not begin with the file
    public FileManager(int totalSize, int pieceSize) {
        System.out.println("This peer does not have the file. Its file is empty.");
        this.pieceSize = pieceSize;
        int numPieces = (int) Math.ceil(totalSize / (double) pieceSize);
        pieces = new byte[numPieces][];
        hasPiece = new boolean[numPieces]; // without the file, we obviously begin lacking the piece
    }
}

