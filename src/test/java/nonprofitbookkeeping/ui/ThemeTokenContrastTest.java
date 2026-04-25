package nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * Verifies key theme token combinations meet WCAG contrast expectations.
 */
class ThemeTokenContrastTest
{
	private static final double MIN_CONTRAST = 4.5;
	private static final Pattern TOKEN_PATTERN =
		Pattern.compile("\\s*(-[\\w-]+):\\s*([^;]+);");

	@Test
	void pendingRowContrastIsAccessibleInLightTheme() throws IOException
	{
		assertPendingRowContrast("themes/light.css", "light");
	}

	@Test
	void pendingRowContrastIsAccessibleInDarkTheme() throws IOException
	{
		assertPendingRowContrast("themes/dark.css", "dark");
	}

	@Test
	void statusBadgeContrastIsAccessibleInLightTheme() throws IOException
	{
		assertStatusContrast("themes/light.css", "light");
	}

	@Test
	void statusBadgeContrastIsAccessibleInDarkTheme() throws IOException
	{
		assertStatusContrast("themes/dark.css", "dark");
	}

	@Test
	void validationErrorContrastIsAccessibleInLightTheme() throws IOException
	{
		assertValidationContrast("themes/light.css", "light");
	}

	@Test
	void validationErrorContrastIsAccessibleInDarkTheme() throws IOException
	{
		assertValidationContrast("themes/dark.css", "dark");
	}

	private void assertPendingRowContrast(String resourcePath, String theme)
		throws IOException
	{
		Map<String, String> tokens = readTokens(resourcePath);
		String pendingBgToken = tokens.get("-npbk-pending-row-bg");
		String pendingTextToken = tokens.get("-npbk-pending-row-text");
		String surfaceToken = tokens.get("-npbk-surface-default");

		assertNotNull(pendingBgToken,
			"Missing -npbk-pending-row-bg token in " + resourcePath);
		assertNotNull(pendingTextToken,
			"Missing -npbk-pending-row-text token in " + resourcePath);
		assertNotNull(surfaceToken,
			"Missing -npbk-surface-default token in " + resourcePath);

		Color pendingBg = parseColor(pendingBgToken);
		Color surface = parseColor(surfaceToken);
		Color effectiveBackground = pendingBg.withAlphaBlendedOver(surface);
		Color text = parseColor(pendingTextToken);

		double contrast = contrastRatio(effectiveBackground, text);
		assertTrue(contrast >= MIN_CONTRAST,
			() -> String.format(
				"Expected pending row contrast >= %.1f in %s theme but got %.2f",
				MIN_CONTRAST, theme, contrast));
	}

	private static Map<String, String> readTokens(String resourcePath)
		throws IOException
	{
		Map<String, String> tokens = new HashMap<>();
		InputStream in = ThemeTokenContrastTest.class.getClassLoader()
			.getResourceAsStream(resourcePath);
		if (in == null)
		{
			throw new IOException("Resource not found: " + resourcePath);
		}

		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(in, StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				Matcher matcher = TOKEN_PATTERN.matcher(line);
				if (matcher.find())
				{
					tokens.put(matcher.group(1), matcher.group(2).trim());
				}
			}
		}
		return tokens;
	}

	private void assertStatusContrast(String resourcePath, String theme)
		throws IOException
	{
		Map<String, String> tokens = readTokens(resourcePath);
		String statusTextToken = tokens.get("-npbk-status-text");
		assertNotNull(statusTextToken,
			"Missing -npbk-status-text token in " + resourcePath);

		Color statusText = parseColor(statusTextToken);
		assertContrast(tokens, theme, resourcePath, statusText,
			"-npbk-status-neutral-bg", "neutral badge");
		assertContrast(tokens, theme, resourcePath, statusText,
			"-npbk-status-success-bg", "success badge");
		assertContrast(tokens, theme, resourcePath, statusText,
			"-npbk-status-error-bg", "error badge");
	}

	private void assertValidationContrast(String resourcePath, String theme)
		throws IOException
	{
		Map<String, String> tokens = readTokens(resourcePath);
		String validationToken = tokens.get("-npbk-validation-error");
		String surfaceToken = tokens.get("-npbk-surface-default");
		assertNotNull(validationToken,
			"Missing -npbk-validation-error token in " + resourcePath);
		assertNotNull(surfaceToken,
			"Missing -npbk-surface-default token in " + resourcePath);

		Color validationText = parseColor(validationToken);
		Color surface = parseColor(surfaceToken);
		double contrast = contrastRatio(surface, validationText);
		assertTrue(contrast >= MIN_CONTRAST,
			() -> String.format(
				"Expected validation contrast >= %.1f in %s theme but got %.2f",
				MIN_CONTRAST, theme, contrast));
	}

	private void assertContrast(Map<String, String> tokens, String theme,
		String resourcePath, Color textColor, String backgroundToken,
		String label)
	{
		String rawBackgroundToken = tokens.get(backgroundToken);
		assertNotNull(rawBackgroundToken,
			"Missing " + backgroundToken + " token in " + resourcePath);
		Color background = parseColor(rawBackgroundToken);
		double contrast = contrastRatio(background, textColor);
		assertTrue(contrast >= MIN_CONTRAST,
			() -> String.format(
				"Expected %s contrast >= %.1f in %s theme but got %.2f",
				label, MIN_CONTRAST, theme, contrast));
	}

	private static Color parseColor(String raw)
	{
		String value = raw.trim();
		if (value.startsWith("#"))
		{
			return Color.fromHex(value);
		}
		if (value.toLowerCase().startsWith("rgba("))
		{
			String inner = value.substring(5, value.length() - 1);
			String[] parts = inner.split(",");
			return new Color(
				Integer.parseInt(parts[0].trim()) / 255.0,
				Integer.parseInt(parts[1].trim()) / 255.0,
				Integer.parseInt(parts[2].trim()) / 255.0,
				Double.parseDouble(parts[3].trim()));
		}
		throw new IllegalArgumentException("Unsupported color format: " + raw);
	}

	private static double contrastRatio(Color bg, Color fg)
	{
		double l1 = relativeLuminance(bg);
		double l2 = relativeLuminance(fg);
		double lighter = Math.max(l1, l2);
		double darker = Math.min(l1, l2);
		return (lighter + 0.05) / (darker + 0.05);
	}

	private static double relativeLuminance(Color color)
	{
		double r = toLinear(color.r);
		double g = toLinear(color.g);
		double b = toLinear(color.b);
		return 0.2126 * r + 0.7152 * g + 0.0722 * b;
	}

	private static double toLinear(double channel)
	{
		return channel <= 0.04045 ? channel / 12.92
			: Math.pow((channel + 0.055) / 1.055, 2.4);
	}

	private static final class Color
	{
		private final double r;
		private final double g;
		private final double b;
		private final double a;

		private Color(double r, double g, double b, double a)
		{
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
		}

		private static Color fromHex(String hex)
		{
			String normalized = hex.substring(1);
			if (normalized.length() != 6)
			{
				throw new IllegalArgumentException("Expected 6-digit hex: " + hex);
			}
			int rgb = Integer.parseInt(normalized, 16);
			double r = ((rgb >> 16) & 0xFF) / 255.0;
			double g = ((rgb >> 8) & 0xFF) / 255.0;
			double b = (rgb & 0xFF) / 255.0;
			return new Color(r, g, b, 1.0);
		}

		private Color withAlphaBlendedOver(Color background)
		{
			double outR = this.r * this.a + background.r * (1 - this.a);
			double outG = this.g * this.a + background.g * (1 - this.a);
			double outB = this.b * this.a + background.b * (1 - this.a);
			return new Color(outR, outG, outB, 1.0);
		}
	}
}
