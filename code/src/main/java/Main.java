
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class Main {

    public static void main(String[] args) {
        GZIPInputStream a = null;
        try {
            FileInputStream inputStream = new FileInputStream(args[0]);
            a = new GZIPInputStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        byte[] data = new byte[1];
        int i = 1, bytesRead = 0;
        try {
            while ((bytesRead = a.read(data, 0, 1)) != -1) {
                i = data[0] + i*2;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            a.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
