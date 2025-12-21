package util;

public class GenerateEncryptedString {
	public static void main(String[] args) {
		String email= "rajendra.singh068@gmail.com";
		System.out.println(SimpleCrypto.encrypt(email));
	    String pw = "cddd xhrc wogd rouu"; // app password
	    System.out.println(SimpleCrypto.encrypt(pw));
	}

}
