package org.mtransit.parser.ca_durham_region_transit_bus;

import static org.mtransit.commons.Constants.EMPTY;
import static org.mtransit.commons.Constants.SPACE_;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MTrip;

import java.util.Locale;
import java.util.regex.Pattern;

// http://opendata.durham.ca/
// https://maps.durham.ca/OpenDataGTFS/GTFS_Durham_TXT.zip
// http://opendata.durham.ca/datasets?t=Durham%20Transportation
// https://maps.durham.ca/OpenDataGTFS/GTFS_Durham_TXT.zip
public class DurhamRegionTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new DurhamRegionTransitBusAgencyTools().start(args);
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "DRT";
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String cleanStopOriginalId(@NotNull String gStopId) {
		gStopId = CleanUtils.cleanMergedID(gStopId);
		return gStopId;
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern STARTS_WITH_LETTER = Pattern.compile("(^[a-z] )", Pattern.CASE_INSENSITIVE);

	@Nullable
	@Override
	public String selectDirectionHeadSign(@Nullable String headSign1, @Nullable String headSign2) {
		if (StringUtils.equals(headSign1, headSign2)) {
			return null; // can NOT select
		}
		final boolean startsWithLetter1 = headSign1 != null && STARTS_WITH_LETTER.matcher(headSign1).find();
		final boolean startsWithLetter2 = headSign2 != null && STARTS_WITH_LETTER.matcher(headSign2).find();
		if (startsWithLetter1) {
			if (!startsWithLetter2) {
				return headSign2;
			}
		} else if (startsWithLetter2) {
			return headSign1;
		}
		return null;
	}

	@Nullable
	@Override
	public String mergeComplexDirectionHeadSign(@Nullable String headSign1, @Nullable String headSign2) {
		if (StringUtils.equals(headSign1, headSign2)) {
			return null; // can NOT select
		}
		if (headSign1 == null || headSign2 == null) {
			return null;
		}
		final boolean startsWithLetter1 = STARTS_WITH_LETTER.matcher(headSign1).find();
		final boolean startsWithLetter2 = STARTS_WITH_LETTER.matcher(headSign2).find();
		if (startsWithLetter1 && startsWithLetter2) {
			return MTrip.mergeHeadsignValue(
					headSign1.substring(2),
					headSign2.substring(2)
			);
		}
		return null;
	}

	private static final Pattern DASH_ = Pattern.compile("( - | -|- )", Pattern.CASE_INSENSITIVE);

	private static final Pattern START_WITH_RSN = Pattern.compile("(^[\\d]+)", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_LETTER_ = Pattern.compile("(^([A-Z])\\s?-\\s?)");
	private static final String STARTS_WITH_LETTER_REPLACEMENT = "$2 ";

	private static final Pattern LATE_NIGHT_SHUTTLE_ = CleanUtils.cleanWords("late night shuttle");

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign, getIgnoredWords());
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = DASH_.matcher(tripHeadsign).replaceAll(SPACE_);
		tripHeadsign = START_WITH_RSN.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = STARTS_WITH_LETTER_.matcher(tripHeadsign).replaceAll(STARTS_WITH_LETTER_REPLACEMENT);
		tripHeadsign = LATE_NIGHT_SHUTTLE_.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
		tripHeadsign = CleanUtils.CLEAN_AT.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@NotNull
	private String[] getIgnoredWords() {
		return new String[]{
				"OPG", "UOIT",
		};
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, gStopName, getIgnoredWords());
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.cleanSlashes(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		final String stopCode = gStop.getStopCode();
		if (!StringUtils.isEmpty(stopCode)
				&& CharUtils.isDigitsOnly(stopCode)) {
			return Integer.parseInt(stopCode); // use stop code as stop ID
		}
		//noinspection deprecation
		final String stopId = CleanUtils.cleanMergedID(gStop.getStopId());
		if (stopId.equalsIgnoreCase("Stro Tree1:1")) {
			return 1000001546;
		} else if (stopId.equalsIgnoreCase("King Live3:1")) {
			return 1000001609;
		} else if (stopId.equalsIgnoreCase("Came Ann1:1")) {
			return 93549;
		} else if (stopId.equalsIgnoreCase("Audl Horn:1")) {
			return 1000000001;
		} else if (stopId.equalsIgnoreCase("OldK Eliz:1")) {
			return 1000000002;
		} else if (stopId.equalsIgnoreCase("Stev Crei1:1")) {
			return 1000000003;
		} else if (stopId.equalsIgnoreCase("Harm Capr:1")) {
			return 1000000004;
		} else if (stopId.equalsIgnoreCase("Park Buen:1")) {
			return 1000000005;
		} else if (stopId.equalsIgnoreCase("Main Duch:1")) {
			return 1000000006;
		} else if (stopId.equalsIgnoreCase("Main Wint:1")) {
			return 1000000007;
		} else if (stopId.equalsIgnoreCase("Adel Came:1")) {
			return 1000000008;
		} else if (stopId.equalsIgnoreCase("Broc Quak:1")) {
			return 1000000009;
		} else if (stopId.equalsIgnoreCase("Main Dall:1")) {
			return 1000000010;
		} else if (stopId.equalsIgnoreCase("Audl Horn1:1")) {
			return 1000000011;
		} else if (stopId.equalsIgnoreCase("Wils Shak1:1")) {
			return 1000000012;
		} else if (stopId.equalsIgnoreCase("Wils Shak:1")) {
			return 1000000013;
		} else if (stopId.equalsIgnoreCase("Base RR5711")) {
			return 1000000014;
		} else if (stopId.equalsIgnoreCase("Shoa Marj11")) {
			return 1000000015;
		} else if (stopId.equalsIgnoreCase("Ross Chur1")) {
			return 3430;
		} else if (stopId.equalsIgnoreCase("Ross South11")) {
			return 1000000017;
		} else if (stopId.equalsIgnoreCase("Audl Dona21")) {
			return 1000000018;
		} else if (stopId.equalsIgnoreCase("Ross SouthW1")) {
			return 1000000019;
		} else if (stopId.equalsIgnoreCase("Alto Sha21")) {
			return 1000000020;
		} else if (stopId.equalsIgnoreCase("Segg Brad1")) {
			return 1000000021;
		} else if (stopId.equalsIgnoreCase("Segg Armi1")) {
			return 1000000022;
		} else if (stopId.equalsIgnoreCase("Segg West1")) {
			return 1000000023;
		} else if (stopId.equalsIgnoreCase("Segg Good1")) {
			return 1000000024;
		} else if (stopId.equalsIgnoreCase("Segg Will1")) {
			return 1000000025;
		} else if (stopId.equalsIgnoreCase("Aud Denn1")) {
			return 1000000026;
		} else if (stopId.equalsIgnoreCase("Aud Denn21")) {
			return 1000000027;
		} else if (stopId.equalsIgnoreCase("Carn Bald11")) {
			return 1000000028;
		} else if (stopId.equalsIgnoreCase("Carn Brad1")) {
			return 1000000029;
		} else if (stopId.equalsIgnoreCase("Carn Way1")) {
			return 1000000030;
		} else {
			return Math.abs(stopId.hashCode());
		}
	}
}
