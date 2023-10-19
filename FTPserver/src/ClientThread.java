import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class ClientThread extends Thread {
    private static final String key = "aesEncryptionKey";
    private static final String initVector = "encryptionIntVec";

    public Socket client = null;
    public DataInputStream dis = null;
    public DataOutputStream dos = null;
    public FileInputStream fis = null;
    public FileOutputStream fos = null;

    public File file = null;

    public ClientThread(Socket c) {
        try {
            client = c;
            dis = new DataInputStream(c.getInputStream());
            dos = new DataOutputStream(c.getOutputStream());
        } catch (Exception e) {

        }
    }

    public void run() {
        while (true) {
            try {
                DataInputStream in = new DataInputStream(client.getInputStream());
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                // send the client services info and get their input
                out.writeUTF("1. Download file \n2. Upload file\n3. List files");
                String input = dis.readUTF();
                String filename = "", filedata = "";
                byte[] data;
                if (input.equals("2")) {
                    filename = dis.readUTF();
                    filedata = dis.readUTF();
                    //decrypt file data
                    String decryptedFile = decrypt(filedata);
                    fos = new FileOutputStream(filename);
                    fos.write(decryptedFile.getBytes());
                    fos.close();
                } else if (input.equals("1")) {
                    filename = dis.readUTF();
                    file = new File(filename);
                    if (file.isFile()) {
                        fis = new FileInputStream(file);
                        data = new byte[fis.available()];
                        fis.read(data);
                        filedata = new String(data);
                        fis.close();
                        //encrypt file data
                        String establishConnection = encrypt(filedata);
                        dos.writeUTF(establishConnection);
                    } else {
                        dos.writeUTF("");
                    }
                } else if (input.equals("3")) {
                    File currentDirectory = new File(System.getProperty("user.dir"));
                    File[] files = currentDirectory.listFiles();
                    StringBuilder fileList = new StringBuilder();

                    for (File file : files) {
                        if (file.isFile()) {
                            fileList.append(file.getName()).append("\n");
                        }
                    }

                    dos.writeUTF(fileList.toString());
                } else {
                    System.out.println("Error");
                }
            } catch (Exception e) {

            }
        }
    }


    private static String decrypt(String data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] encrypted = Base64.getDecoder().decode(data);

        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes());
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

        byte[] decrypted = cipher.doFinal(encrypted);

        String s = new String(decrypted, StandardCharsets.UTF_8);

        return s;
    }
    private static String encrypt(String data) throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException {
        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes());
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv); // or Cipher.DECRYPT_MODE

        byte[] encrypted = cipher.doFinal(data.getBytes());

        String s = Base64.getEncoder().encodeToString(encrypted);
        System.out.println(s);
        return s;
    }
}
