package de.jowo.pspac.masks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CalcMaskCombinations {

	Path masksDir = Paths.get("G:\\Studium_Master\\Sem_2_SS17\\PS-PAC\\Project\\hashcat-3.5.0\\masks\\");

	static Map<String, Integer> maskToCombinations = new HashMap<>();

	@BeforeClass
	public static void setup() {
		maskToCombinations.put("?u", "ABCDEFGHIJKLMNOPQRSTUVWXYZ".length());
		maskToCombinations.put("?l", "abcdefghijklmnopqrstuvwxyz".length());
		maskToCombinations.put("?d", "0123456789".length());
		maskToCombinations.put("?s", "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~".length());
		maskToCombinations.put("?h", "0123456789abcdef".length());
		maskToCombinations.put("?H", "0123456789ABCDEF".length());
	}

	public void estimateFile(Path maskfile) {
		List<String> lines;
		try {
			lines = Files.readAllLines(maskfile);
		} catch (IOException e) {
			System.out.println("Failed to read: " + maskfile);
			return;
		}

		long sum = lines.stream().mapToLong(line -> {
			long num = calculateCombinations(line);
			// System.out.println(num + " - " + line);
			return num;
		}).sum();

		System.out.println(String.format("%,d \t\t %s", sum, maskfile));
	}

	@Test
	public void estimateAll() throws IOException {
		Files.list(masksDir).forEach(this::estimateFile);
	}

	@Test
	public void test() {
		long num = calculateCombinations("?u?l?l?l?l?l?d?d");

		Assert.assertEquals(30891577600L, num);
	}

	public long calculateCombinations(String line) {
		long num = 1;
		for (int i = 0; i < line.length() - 1; i += 2) {
			String mask = String.valueOf(line.charAt(i)) + String.valueOf(line.charAt(i + 1));
			num *= maskToCombinations.get(mask);
		}
		return num;
	}
}
