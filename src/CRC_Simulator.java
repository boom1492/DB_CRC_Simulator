import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.zip.CRC32;

public class CRC_Simulator {
	
	private static int KILO = 1024;
	private static int CRC1 = 1, CRC8 = 8, CRC16 = 16, CRC32 = 32;
	
	private FileInputStream fis = null;
	
	public static void main(String arg[]){
		
		//이하 test code
		CRC_Simulator crc = new CRC_Simulator();
		crc.simulate(CRC8, 1000);
		crc.simulate(CRC16, 1000);
		crc.simulate(CRC32, 1000);
		crc.simulate(CRC8, 10000);
		crc.simulate(CRC16, 10000);
		crc.simulate(CRC32, 10000);
		crc.simulate(CRC8, 1000000);
		crc.simulate(CRC16, 1000000);
		crc.simulate(CRC32, 1000000);
		
	}
	
	public void simulate(int type, int ep){
		
		openFile();
		
		byte[] buf = new byte[KILO];
		
		int frm_cnt = 0;
		int err_cnt = 0;
		int index = 0;
		if(type==CRC32)
			index=4;
		else if(type==CRC16)
			index=2;
		else if(type==CRC8)
			index=1;
		else
			index=0;
		
		byte[] crcbuf = new byte[KILO+index];
		
		
		while(true){
			buf = readByte();
			if(buf==null)
				break;
			
			for(int i=0;i<buf.length;i++){
				crcbuf[i] = buf[i];
			}

			for(int i=0;i<index;i++){
				crcbuf[1024+i] = generateCRC(crcbuf, type)[i];
			}

			byte[] transbuf = transmitChannel(crcbuf, ep);
			frm_cnt++;
			//debugging code
			int berr_cnt=0;
			for(int i=0;i<KILO;i++){
				if(!Integer.toHexString(crcbuf[i]).equals(Integer.toHexString(transbuf[i]))){
					int t1 = (crcbuf[i]>0) ? crcbuf[i] : crcbuf[i]+256;
					int t2 = (transbuf[i]>0) ? transbuf[i] : transbuf[i]+256;
					
					//System.out.println("Error> before : " + Integer.toBinaryString(t1) + ", after : " + Integer.toBinaryString(t2));
					berr_cnt++;
				}
			}
			if(berr_cnt!=0) err_cnt++;
		}
		System.out.println("---------------------------------------------------------");
		System.out.println("case) CRC-" + type + ", bit error rate : 1/" + ep);
		System.out.println(frm_cnt + "번의 Frame 전송 중 에러검출된 Frame " + err_cnt + "개 발견");

	}
	
	public byte[] transmitChannel(byte[] data, int ep){
		int[] temp = new int[data.length];
		for(int i=0;i<data.length;i++){
			temp[i] = data[i];
			if(temp[i]<0) temp[i]+=256;
		}
		Random rand = new Random();
		Random rand2 = new Random();
		for(int i=0;i<temp.length;i++){
			for(int j=0;j<8;j++){
				int r = rand2.nextInt(8);
				if(rand.nextInt(ep)==0){
					switch(r){
					case 0: temp[i] ^= 0x01;break;
					case 1: temp[i] ^= 0x02;break;
					case 2: temp[i] ^= 0x04;break;
					case 3: temp[i] ^= 0x08;break;
					case 4: temp[i] ^= 0x10;break;
					case 5: temp[i] ^= 0x20;break;
					case 6: temp[i] ^= 0x40;break;
					case 7: temp[i] ^= 0x80;break;
					}
				}
			}
		}
		byte[] ret = new byte[data.length];
		for(int i=0;i<data.length;i++){
			ret[i] = (byte) temp[i];
		}
		return ret;
		
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
		byte[] buf = new byte[KILO];

		try {
			fis.read(buf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("파일을 읽지 못했습니다.");
			return null;
		}
		for(int i=0;i<KILO;i++){
			if(buf[i]!=0){
				break;
			}
			return null;
		}
		return buf;
	}

	public byte[] generateCRC(byte[] data, int type){
		if(type==CRC1){
			//x + 1
			
		} 
		else if(type==CRC8){
			CRC8 crc8 = new CRC8();
			crc8.update(data);
			int temp =  (int) crc8.getValue();
			byte[] crc = new byte[1];
			crc[0] = (byte) (temp & 0xFF);

			return crc;
		} 
		else if(type==CRC16){
			CRC16 crc16 = new CRC16();
			crc16.update(data);
			int temp =  (int) crc16.getValue();
			byte[] crc = new byte[2];
			crc[1] = (byte) (temp & 0x00FF);
			crc[0] = (byte) ((temp & 0xFF00) >> 8);

			return crc;
		}
		else if(type==CRC32){
			CRC32 crc32 = new CRC32();
			crc32.update(data);
			int temp =  (int) crc32.getValue();
			byte[] crc = new byte[4];
			crc[3] = (byte) (temp & 0x000000FF);
			crc[2] = (byte) ((temp & 0x0000FF00) >> 8);
			crc[1] = (byte) ((temp & 0x00FF0000) >> 16);
			crc[0] = (byte) ((temp & 0xFF000000) >> 24);

			return crc;
		}
		
		return null;

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
