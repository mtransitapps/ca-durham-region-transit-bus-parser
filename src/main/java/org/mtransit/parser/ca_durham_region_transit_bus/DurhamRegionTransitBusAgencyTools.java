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
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;

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

	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteLongName())) {
			if ("101A".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return "Industrial Bay Ridges";
			} else if ("219C".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return "Ravenscroft";
			} else if ("232".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return "Church";
			} else if ("308C".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return "Whitby Shores";
			} else if ("310".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return "Brooklin / UOIT & DC";
			} else if ("401C".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return "Simcoe";
			} else if ("417".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return "Conlin";
			} else if ("506".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return "Wilmot Crk Orono Newcastle"; // Community Bus
			} else if ("910C".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return "Campus Connect";
			}
			throw new MTLog.Fatal("Unexpected route long name for %s!", gRoute);
		}
		return super.getRouteLongName(gRoute);
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute, @NotNull MAgency agency) {
		if (StringUtils.isEmpty(gRoute.getRouteColor())) {
			final String rsnS = gRoute.getRouteShortName();
			if (CharUtils.isDigitsOnly(rsnS)) {
				final int rsn = Integer.parseInt(rsnS);
				switch (rsn) {
				// @formatter:off
				case 101: return "6345A6";
				case 103: return "F2C61D";
				case 107: return "4866BA";
				case 110: return "ED1C24";
				case 111: return "2C3792";
				case 112: return "2F684C";
				case 120: return "CC8ACB";
				case 181: return "A9A9A9"; // ???
				case 182: return "A9A9A9"; // ???
				case 193: return "00ADEF";
				case 199: return "AC74C1";
				case 215: return "9587B7";
				case 216: return "754C29";
				case 217: return "0075BF";
				case 218: return "988FD0";
				case 219: return "4D71C1";
				case 221: return "F3B722";
				case 222: return "54C6BD";
				case 223: return "602C83";
				case 224: return "64C430";
				case 225: return "9A8D7D";
				case 226: return "6345A6";
				case 232: return "EE2428";
				case 283: return "A9A9A9"; // ???
				case 284: return "A9A9A9"; // ???
				case 291: return "33B652";
				case 301: return "8D3CA3";
				case 302: return "EE2428";
				case 303: return "6EC95F";
				case 304: return "79D0ED";
				case 305: return "8448a9";
				case 308: return "AC57B2";
				case 310: return "DFC463";
				case 312: return "AAA8A9";
				case 318: return "9BA821";
				case 385: return "A9A9A9"; // ???
				case 401: return "EE2428";
				case 402: return "6E2932";
				case 403: return "4E5BB3";
				case 405: return "C6A218";
				case 406: return "00ADEF";
				case 407: return "AAA8A9";
				case 408: return "ED1C24";
				case 409: return "4159B1";
				case 410: return "A14A94";
				case 411: return "536DBE";
				case 412: return "85D168";
				case 414: return "CE92CF";
				case 416: return "EC5788";
				case 417: return "74459B";
				case 420: return "324954";
				case 422: return "EA6C14";
				case 501: return "2D4CA3";
				case 502: return "5FC3F0";
				case 506: return "F14823";
				case 601: return "2D4CA3";
				case 603: return "F14623";
				case 653: return "8D188F";
				case 654: return "293D9B";
				case 750: return null; // TODO
				case 752: return null; // TODO
				case 801: return "4EA3DD";
				case 900: return "DE581E"; // PULSE
				case 901: return "DE581E"; // PULSE
				case 902: return null; // TODO
				case 905: return null; // TODO
				case 910: return "75AC95";
				case 915: return "36612B";
				case 916: return "5D1B1E";
				case 917: return null; // TODO
				case 922: return "231F20";
				case 923: return "65A930";
				case 950: return "87CF25";
				case 960: return "B2D235";
				case 980: return null; // TODO
				// @formatter:on
				}
			}
			if ("101A".equalsIgnoreCase(rsnS)) {
				return "6345A6"; // 101
			} else if ("103B".equalsIgnoreCase(rsnS)) {
				return "A7516D";
			} else if ("110A".equalsIgnoreCase(rsnS)) {
				return "ED1C24"; // 110
			} else if ("110B".equalsIgnoreCase(rsnS)) {
				return "ED1C24"; // 110
			} else if ("110sh".equalsIgnoreCase(rsnS)) {
				return "ED1C24"; // 110
			} else if ("111A".equalsIgnoreCase(rsnS)) {
				return "2C3792"; // 111
			} else if ("111S".equalsIgnoreCase(rsnS)) {
				return "2C3792"; // 111
			} else if ("112CS".equalsIgnoreCase(rsnS)) {
				return "2F684C"; // 112
			} else if ("112SH".equalsIgnoreCase(rsnS)) {
				return "2F684C"; // 112
			} else if ("193A".equalsIgnoreCase(rsnS)) {
				return "00ADEF"; // 193
			} else if ("193B".equalsIgnoreCase(rsnS)) {
				return "00ADEF"; // 193
			} else if ("218D".equalsIgnoreCase(rsnS)) {
				return "988FD0"; // 218
			} else if ("219C".equalsIgnoreCase(rsnS)) {
				return "4D71C1"; // 219
			} else if ("219D".equalsIgnoreCase(rsnS)) {
				return "4D71C1"; // 219
			} else if ("219DSH".equalsIgnoreCase(rsnS)) {
				return "4D71C1"; // 219
			} else if ("221D".equalsIgnoreCase(rsnS)) {
				return "F3B722"; // 221
			} else if ("223C".equalsIgnoreCase(rsnS)) {
				return "602C83"; // 223
			} else if ("224D".equalsIgnoreCase(rsnS)) {
				return "64C430"; // 224
			} else if ("224DSH".equalsIgnoreCase(rsnS)) {
				return "64C430"; // 224 //
			} else if ("225A".equalsIgnoreCase(rsnS)) {
				return "9A8D7D"; // 225
			} else if ("226S".equalsIgnoreCase(rsnS)) {
				return "6345A6"; // 226
			} else if ("232S".equalsIgnoreCase(rsnS)) {
				return "EE2428"; // 232
			} else if ("308C".equalsIgnoreCase(rsnS)) {
				return "AC57B2"; // 308
			} else if ("318SH".equalsIgnoreCase(rsnS)) {
				return "9BA821"; // 318
			} else if ("401B".equalsIgnoreCase(rsnS)) {
				return "8AD9F2";
			} else if ("401C".equalsIgnoreCase(rsnS)) {
				return "6C57B1";
			} else if ("402B".equalsIgnoreCase(rsnS)) {
				return "9BA821";
			} else if ("407S".equalsIgnoreCase(rsnS)) {
				return "AAA8A9"; // 407
			} else if ("407C".equalsIgnoreCase(rsnS)) {
				return "AAA8A9"; // 407
			} else if ("407CS".equalsIgnoreCase(rsnS)) {
				return "AAA8A9"; // 407
			} else if ("410S".equalsIgnoreCase(rsnS)) {
				return "A14A94"; // 410
			} else if ("411S".equalsIgnoreCase(rsnS)) {
				return "536DBE"; // 411
			} else if ("412S".equalsIgnoreCase(rsnS)) {
				return "85D168"; // 412
			} else if ("412CS".equalsIgnoreCase(rsnS)) {
				return "85D168"; // 412
			} else if ("910C".equalsIgnoreCase(rsnS)) {
				return "9BA821";
			} else if ("922B".equalsIgnoreCase(rsnS)) {
				return "3DA87F";
			}
			throw new MTLog.Fatal("Unexpected route color %s!", gRoute.toStringPlus());
		}
		return super.getRouteColor(gRoute, agency);
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
