import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
public class Client{
    private static final String key = "aesEncryptionKey";
    private static final String initVector = "encryptionIntVec";
    public Socket client = null;
    public DataInputStream dis = null;
    public DataOutputStream dos = null;
    public FileInputStream fis = null;
    public FileOutputStream fos = null;
    public BufferedReader buffer = null;
    public String userInput = "";
    public static void main(String[] args)
    {
        Client client = new Client();
        client.establishConnection();
    }
    public void establishConnection()
    {
        try{
            // establish the TCP connection and create  in and out streams
            InputStreamReader isr = new InputStreamReader(System.in);
            buffer = new BufferedReader(isr);
            client = new Socket("127.0.0.1",1234);
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());
            // read data from the server
            String serverMessage = dis.readUTF();
            System.out.println(serverMessage);
            String username = buffer.readLine();
            dos.writeUTF(username);
            String serverMessage2 = dis.readUTF();
            System.out.println(serverMessage2);
            String password = buffer.readLine();
            dos.writeUTF(password);

        }
        catch(Exception e)
        {
            System.out.println("Unable to Connect to Server");
        }
        while(true)
        {
            try{
                // show server's services
                String msg= dis.readUTF();
                System.out.println(msg);
                // reconnect if username or password are incorrect
                if (msg.equals("user name or password are incorrect")){
                    Client client = new Client();
                    client.establishConnection();
                }
                // handle user selection and inform the server
                userInput = buffer.readLine();
                dos.writeUTF(userInput);
                int i = Integer.parseInt(userInput);
                if (i == 1 ){
                    download();
                }
                else if (i == 2) {
                    upload();
                } else if (i ==3) {
                    System.out.println(dis.readUTF());
                } else {
                    System.out.println("please choose from the list!");
                }
            }
            catch(Exception e)
            {
                System.out.println("something went wrong! ");
                break;
            }
        }
    }
    public void upload() {
        try{
            String filename="",filedata="";
            File file;
            byte[] data;
            System.out.println("Enter file name to upload: ");
            filename = buffer.readLine();
            file = new File(filename);
            if(file.isFile())
            {
                fis = new FileInputStream(file);
                data = new byte[fis.available()];
                fis.read(data);
                fis.close();
                filedata = new String(data);
                dos.writeUTF(filename);
                //encrypting file data
                String encryptedFile = encrypt(filedata);
                dos.writeUTF(encryptedFile);
                System.out.println("File Send Successful!");
            }
            else
            {
                System.out.println("File not found!");
            }
        }
        catch(Exception e)
        {
        }
    }
    public void download(){
        try{
            String filename="",filedata="";
            System.out.println("Enter file name: ");
            filename = buffer.readLine();
            dos.writeUTF(filename);
            filedata = dis.readUTF();
            //decrypt file data
            String decryptedFile = decrypt(filedata);
            if(decryptedFile.equals(""))
            {
                System.out.println("File not found!");
            }
            else
            {
                fos = new FileOutputStream(filename);
                fos.write(decryptedFile.getBytes());
                System.out.println("File downloaded");
                fos.close();
            }
        }
        catch(Exception e)
        {
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