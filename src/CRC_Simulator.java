import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.BitSet;


public class CRC_Simulator {
	
	private FileInputStream fis = null;
	
	public static void main(String arg[]){
		CRC_Simulator crc = new CRC_Simulator();
		crc.cycle();		
		
		BitSet bit = new BitSet(10);
		
	}
	
	public void cycle(){
		
		openFile();
		
		byte[] buf = new byte[1024];
		byte[] crcbuf = new byte[2000];
		while(true){
			buf = readByte();
			if(buf==null)
				break;
			crcbuf = addCRC(buf);
			
		}

	}
	
	public void openFile(){
		File file = new File("./sample.txt");
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("파일을 찾지 못했습니다.");
		}
	}
	
	public byte[] readByte(){
		byte[] buf = new byte[1024];

		try {
			fis.read(buf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("파일을 읽지 못했습니다.");
			return null;
		}
		for(int i=0;i<1024;i++){
			if(buf[i]!=0){
				break;
			}
			return null;
		}
		return buf;
	}

	public byte[] addCRC(byte[] bb)// crc check-16을 반환하는 byte배열
	{
		byte[] barray = new byte[2];
		int Carry;
		int CRC = 0xFFFF;
		for (int i = 0; i < bb.length; i++) {
			int temp = bb[i];
			if (temp < 0)
				temp = ((temp * (-1)) ^ 0xff) + 1;
			CRC = CRC ^ temp;

			for (int j = 0; j < 8; j++) {
				Carry = CRC & 0x0001;
				CRC = (CRC >> 1);
				if (Carry == 1)
					CRC = CRC ^ 0xA001;
			}
		}
		barray[0] = (byte) (CRC & 0x00FF);
		barray[1] = (byte) ((CRC & 0xFF00) >> 8);
		return barray;
	}

	public boolean checkCRC(byte[] bb) {
		byte[] barray = new byte[2];
		int Carry;
		int CRC = 0xFFFF;

		for (int i = 0; i < bb.length; i++) {
			int temp = bb[i];
			if (temp < 0)
				temp = ((temp * (-1)) ^ 0xff) + 1;
			CRC = CRC ^ temp;
			for (int j = 0; j < 8; j++) {
				Carry = CRC & 0x0001;
				CRC = (CRC >> 1);
				if (Carry == 1)
					CRC = CRC ^ 0xA001;
			}
		}
		if (CRC == 0x0000)
			return true;
		else
			return false;

	}

}
