package CombinedPackage;

public class MyFile {
	private int id;
	private String name;
	private byte[] data;
	private byte[] unDecryptedData;
	private String fileExtension;

	public MyFile(int id, String name, byte[] data, String fileExtension, byte[] unDecryptedData) {
		this.id = id;
		this.name = name;
		this.data = data;
		this.fileExtension = fileExtension;
		this.unDecryptedData = unDecryptedData;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setFileExtension(String fileExtention) {
		this.fileExtension = fileExtension;
	}

	public void setUnDecryptedData(byte[] unDecryptedData) {
		this.unDecryptedData = unDecryptedData;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public byte[] getData() {
		return data;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public byte[] getUnDecryptedData() {
		return unDecryptedData;
	}
}
