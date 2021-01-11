package Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

public class Main {
	static String directory = System.getProperty("user.dir");
	static File excelFile = new File(directory + "/InvestingData.xlsx");
	static FileInputStream file;

	static XSSFWorkbook workbook;
	static XSSFSheet dataSheet;
	static XSSFSheet rawSheet;
	static LinkedList<String> companyNames;
	static LinkedList<String> allCompanyNames;

	static HttpRequest request;
	static HttpResponse<String> response;
	static JSONObject httpResponse;
	static JSONArray results;

	public static void main(String[] args) throws Exception {

		initializeExcel();
		allCompanyNames = companyNamesFromExcel(0);
		for (int i = 0; i < 3; i++) {
			companyNames = companyNamesFromExcel(i + 1);
			System.out.println(companyNames);
			// yahooAPIConnect();
			// displayData();
		}

		for (String name : allCompanyNames) {
			Row row = rawSheet.createRow(rawSheet.getLastRowNum() + 1);
			row.createCell(0).setCellValue(name);
			row.createCell(1).setCellValue(new Timestamp(System.currentTimeMillis()).toString());
			FileOutputStream outputStream = new FileOutputStream(excelFile);
			workbook.write(outputStream);
		}

		workbook.close();
		file.close();

	}

	static void yahooAPIConnect() throws IOException, InterruptedException {
		request = HttpRequest.newBuilder()
				.uri(URI.create(
						"https://apidojo-yahoo-finance-v1.p.rapidapi.com/market/v2/get-quotes?region=US&symbols="
								+ namesToHTTPS()))// AMD%2CIBM%2CAAPL
				.header("x-rapidapi-key", "a4bef36940msh85e1e27c66ca1dap144d70jsnea16ded5f5d7")
				.header("x-rapidapi-host", "apidojo-yahoo-finance-v1.p.rapidapi.com")
				.method("GET", HttpRequest.BodyPublishers.noBody()).build();
		response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		httpResponse = new JSONObject(response.body());
		results = httpResponse.getJSONObject("quoteResponse").getJSONArray("result");

	}

	static void displayData() {
		for (int i = 0; i < results.length(); i++) {
			try {
				float price = httpResponse.getJSONObject("quoteResponse").getJSONArray("result").getJSONObject(i)
						.getFloat("regularMarketPrice");
				float targetMean = httpResponse.getJSONObject("quoteResponse").getJSONArray("result").getJSONObject(i)
						.getFloat("targetPriceMean");

				System.out.format("%8s:  Price: %03.3f  Target: %03.3f  %d%n", companyNames.get(i), price, targetMean,
						i);
			} catch (Exception e) {
				System.out.format("%8s: %d%n", companyNames.get(i), i);
			}

		}
	}

	static String namesToHTTPS() {
		String toHTTPS = "";
		for (String name : companyNames) {
			if (toHTTPS.equals("")) {
				toHTTPS = toHTTPS + name;
			} else if (name.equals("")) {

			} else {
				toHTTPS = toHTTPS + "%2C" + name;
			}
		}
		return toHTTPS;

	}

	static LinkedList<String> companyNamesFromExcel(int columnNumber) {
		LinkedList<String> companyNames = new LinkedList<>();

		Iterator<Row> iterator = dataSheet.iterator();
		iterator.next();
		while (iterator.hasNext()) {
			Row row = iterator.next();
			Cell cell = row.getCell(columnNumber);
			if (cell != null && !cell.getStringCellValue().isEmpty()) {
				companyNames.add(cell.getStringCellValue());
			}
		}
		return companyNames;
	}

	static void initializeExcel() throws IOException {
		excelFile = new File(directory + "/InvestingData.xlsx");
		file = new FileInputStream(excelFile);
		workbook = new XSSFWorkbook(file);
		dataSheet = workbook.getSheetAt(0);
		rawSheet = workbook.getSheetAt(1);
	}
}
