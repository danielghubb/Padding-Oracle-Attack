import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;


public class padding {  
  
    public static void main(String args[]) throws IOException {
    
    //transformieren der Angabe von url über base64 zu input
    String input = "2QicDQHnGmRuZys0M5JcwCSTeFNXvVm%2FSsG1vaEkIZU1OiGgpLJTdbRO2beA831a0xsatfOy01N38W1RidzrXA%3D%3D";
	byte[] inputDecoded = Base64.getDecoder().decode(URLDecoder.decode(input, StandardCharsets.US_ASCII));

    //bestimmen der blocklaengen
	int bytesInBlock = 16;
    int numberBytes = inputDecoded.length;
    int numberBlock = numberBytes / bytesInBlock;

    List<byte[]> arrayBlank = new ArrayList<>(numberBlock - 1);
    byte[] inputDecodedNeu = inputDecoded.clone();

    //eigentliches decodieren siehe VL Material
    int zeichenAscii = 255;
    for (int i = numberBlock; i > 1; i--){
        byte[] byteBlank = new byte[bytesInBlock];
        byte[] inputPadding = inputDecodedNeu.clone();
        int ende;

        if(i>1){
            ende =inputPadding.length - bytesInBlock - 1;
        }else{
            ende = bytesInBlock - 1;
        }

        for (int j = bytesInBlock - 1; j >= 0; j--){
            int value = bytesInBlock - j;
            byte xor = (byte) ((inputDecodedNeu[ende] ^ value) & 0xff);
            int nulle = 0;
            boolean nullByte = false;

            for (int l = 0; l < zeichenAscii; l++){

                inputPadding[ende] = (byte) ((xor ^ l) & 0xff);
                //Erstellen des request und senden an den angegebenen Server
                String urlEncoded = URLEncoder.encode(Base64.getEncoder().encodeToString(inputPadding),StandardCharsets.US_ASCII);
                URL target = new URL("http://gruenau2.informatik.hu-berlin.de:8888/store_secret/" + urlEncoded);
                HttpURLConnection internet = (HttpURLConnection) target.openConnection();
                internet.setRequestMethod("GET");
                
                //auswerten des response des server, wenn 200 dann padding getroffen
                if (internet.getResponseCode() == 200){
                    if (nullByte){
                        inputPadding[ende - 1] = (byte) (nulle & 0xff);
                        l = -1;
                        nullByte = false;
                        nulle++;
                    }else{
                        nullByte = true;
                        byteBlank[j] = (byte) (l & 0xff);
                    }
                }
            }

            inputPadding = inputDecodedNeu.clone();
            ende--;

            for (int k = ende + 1; k <= (((ende / bytesInBlock) * bytesInBlock) + bytesInBlock - 1); k++){
                int indexNeu = k % bytesInBlock;
                byte encPadding = (byte) ((inputPadding[k] ^ (value + 1) ^ byteBlank[indexNeu]) & 0xff);
                inputPadding[k] = encPadding;
            }
        }
        arrayBlank.add(byteBlank);
        inputDecodedNeu = Arrays.copyOfRange(inputDecodedNeu, 0, inputDecodedNeu.length - bytesInBlock);
    }

    byte[] newByteBlank = new byte[(numberBlock - 1) * bytesInBlock];
    int c = 0;
    for (int n = arrayBlank.size() - 1; n >= 0; n--){
        for (int m = 0; m < bytesInBlock; m++){
            newByteBlank[c++] = arrayBlank.get(n)[m];
        }
    }

    byte[] text = Arrays.copyOfRange(newByteBlank, 0, newByteBlank.length - newByteBlank[newByteBlank.length - 1]);
    System.out.println(new String(text, 0));
      
    }
    
  }