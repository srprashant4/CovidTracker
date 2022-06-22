package com.coronatracker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.coronatracker.model.LocationStats;
import com.coronatracker.service.CoronaDataService;

@Controller
public class HomeController {
	
	@Autowired
	CoronaDataService coronaDataService;
	
	@GetMapping("/")
	public String home(Model model) {
		List<LocationStats> allStats = coronaDataService.getAllStats();
		int totalReportedCases = allStats.stream().mapToInt(stat -> stat.getLatestTotalCases()).sum();
		int totalNewCases = allStats.stream().mapToInt(stat -> stat.getDiffFromPrevDay()).sum(); 	
		model.addAttribute("loactionStats", allStats);
		model.addAttribute("totalReportedCases", totalReportedCases);
		model.addAttribute("totalNewCases", totalNewCases);
		return "home";
	}
	
}
