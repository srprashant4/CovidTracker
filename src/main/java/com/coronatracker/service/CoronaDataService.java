package com.coronatracker.service;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.coronatracker.model.LocationStats;

@Service
public class CoronaDataService {
	
	private String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
	private List<LocationStats> allStats = new ArrayList<LocationStats>();
	
	
	
	public List<LocationStats> getAllStats() {
		return allStats;
	}



	public void setAllStats(List<LocationStats> allStats) {
		this.allStats = allStats;
	}



	@PostConstruct
	@Scheduled(cron = "* * 1 * * *")
	public void fetchVirusData() throws IOException, InterruptedException {
		//This List is created to solve the concurrency issue.
		//It will provide the users with the updated list even the it is not yet populated.
		List<LocationStats> newStats = new ArrayList<LocationStats>();
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(VIRUS_DATA_URL))
					.build();
		//Take the body and return it as a String.
		HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
		//System.out.println(httpResponse.body());
		
		StringReader csvBodyReader = new StringReader(httpResponse.body());
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
		for (CSVRecord record : records) {
			LocationStats locationStats = new LocationStats();
			locationStats.setCountry(record.get("Country/Region"));
			locationStats.setProvince(record.get("Province/State"));
			
			int latestCases = Integer.parseInt(record.get(record.size() - 1));
			int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
			locationStats.setLatestTotalCases(latestCases);
			locationStats.setDiffFromPrevDay(latestCases - prevDayCases);
		    //String state = record.get("Province/State");
		    //System.out.println(state);
			System.out.println(locationStats);
			newStats.add(locationStats);
		}
		this.allStats = newStats;
		int totalReportedCases = allStats.stream().mapToInt(stat -> stat.getLatestTotalCases()).sum();
		System.out.println("---------------------"+totalReportedCases);
	}
}
