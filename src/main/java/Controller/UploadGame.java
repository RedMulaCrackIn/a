package Controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import model.ProductModel;
import model.game;

/**
 * Servlet implementation class AddGame
 */
@WebServlet("/AddGame")
@MultipartConfig()
public class UploadGame extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static String SAVE_DIR = "img";
	static ProductModel GameModels = new ProductModelDM();

	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	LocalDateTime now = LocalDateTime.now();

	public UploadGame() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");

		out.write("Error: GET method is used but POST method is required");
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Definire una lista di estensioni consentite
		final List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");

		Collection<?> games = (Collection<?>) request.getSession().getAttribute("games");
		String savePath = request.getServletContext().getRealPath("") + File.separator + SAVE_DIR;
		game g1 = new game();

		String fileName = null;
		String message = "upload =\n";

		if (request.getParts() != null && request.getParts().size() > 0) {
			for (Part part : request.getParts()) {
				fileName = extractFileName(part);

				if (fileName != null && !fileName.equals("")) {
					// Ottieni l'estensione del file
					String fileExtension = getFileExtension(fileName);

					// Verifica l'estensione
					if (allowedExtensions.contains(fileExtension)) {
						// Verifica il contenuto del file
						if (isValidFileContent(part)) {
							part.write(savePath + File.separator + fileName);
							g1.setImg(fileName);
							message = message + fileName + "\n";
						} else {
							request.setAttribute("error", "Errore: Il contenuto del file non è valido");
						}
					} else {
						request.setAttribute("error", "Errore: Formato file non supportato");
					}
				} else {
					request.setAttribute("error", "Errore: Bisogna selezionare almeno un file");
				}
			}
		}

		g1.setName(request.getParameter("nomeGame"));
		g1.setYears(request.getParameter("years"));
		g1.setAdded(dtf.format(now));
		g1.setQuantity(Integer.valueOf(request.getParameter("quantita")));
		g1.setPEG(Integer.valueOf(request.getParameter("PEG")));
		g1.setIva(Integer.valueOf(request.getParameter("iva")));
		g1.setGenere(request.getParameter("genere"));
		g1.setDesc(request.getParameter("desc"));
		g1.setPrice(Float.valueOf(request.getParameter("price")));

		try {
			GameModels.doSave(g1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		request.setAttribute("stato", "success!");

		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/gameList?page=admin&sort=added DESC");
		dispatcher.forward(request, response);
	}

	private String extractFileName(Part part) {
		// content-disposition: form-data; name="file"; filename="file.txt"
		String contentDisp = part.getHeader("content-disposition");
		String[] items = contentDisp.split(";");
		for (String s : items) {
			if (s.trim().startsWith("filename")) {
				return s.substring(s.indexOf("=") + 2, s.length() - 1);
			}
		}
		return "";
	}

	private String getFileExtension(String fileName) {
		int dotIndex = fileName.lastIndexOf(".");
		return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
	}

	private boolean isValidFileContent(Part part) throws IOException {
		// Magic numbers for jpg, png, and gif
		final byte[] jpgMagic = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
		final byte[] pngMagic = new byte[] { (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47 };
		final byte[] gifMagic = new byte[] { (byte) 0x47, (byte) 0x49, (byte) 0x46 };

		// Read the first few bytes of the file
		InputStream inputStream = part.getInputStream();
		byte[] fileHeader = new byte[4]; // We need to read enough bytes to cover the longest magic number
		inputStream.read(fileHeader);
		inputStream.close();

		// Check the magic number
		if (startsWith(fileHeader, jpgMagic) || startsWith(fileHeader, pngMagic) || startsWith(fileHeader, gifMagic)) {
			return true;
		}
		return false;
	}

	private boolean startsWith(byte[] fileHeader, byte[] magic) {
		for (int i = 0; i < magic.length; i++) {
			if (fileHeader[i] != magic[i]) {
				return false;
			}
		}
		return true;
	}

}