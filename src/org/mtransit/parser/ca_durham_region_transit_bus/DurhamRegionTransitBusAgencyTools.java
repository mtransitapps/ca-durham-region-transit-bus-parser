package org.mtransit.parser.ca_durham_region_transit_bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

// http://www.durham.ca/corpservices.asp?nr=/departments/corpservices/it/gis/opendatalicense.htm&setFooter=/includes/GISfooter.inc
// http://opendata.durham.ca/
// http://opendata.durham.ca/datasets?t=Durham%20Transportation
// https://maps.durham.ca/OpenDataGTFS/GTFS_Durham_TXT.zip
public class DurhamRegionTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-durham-region-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new DurhamRegionTransitBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating DRT bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this, true);
		super.start(args);
		System.out.printf("\nGenerating DRT bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		return super.excludeRoute(gRoute);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	private static final long RID_ENDS_WITH_CS = 3_080_000L;
	private static final long RID_ENDS_WITH_SH = 19_080_000L;

	private static final long RID_ENDS_WITH_A = 10_000L;
	private static final long RID_ENDS_WITH_B = 20_000L;
	private static final long RID_ENDS_WITH_C = 30_000L;
	private static final long RID_ENDS_WITH_D = 40_000L;
	private static final long RID_ENDS_WITH_S = 190_000L;

	@Override
	public long getRouteId(GRoute gRoute) {
		if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			return Long.parseLong(gRoute.getRouteShortName());
		}
		Matcher matcher = DIGITS.matcher(gRoute.getRouteShortName());
		if (matcher.find()) {
			int digits = Integer.parseInt(matcher.group());
			String rsnLC = gRoute.getRouteShortName().toLowerCase(Locale.ENGLISH);
			if (rsnLC.endsWith("cs")) {
				return digits + RID_ENDS_WITH_CS;
			} else if (rsnLC.endsWith("sh")) {
				return digits + RID_ENDS_WITH_SH;
			}
			if (rsnLC.endsWith("a")) {
				return digits + RID_ENDS_WITH_A;
			} else if (rsnLC.endsWith("b")) {
				return digits + RID_ENDS_WITH_B;
			} else if (rsnLC.endsWith("c")) {
				return digits + RID_ENDS_WITH_C;
			} else if (rsnLC.endsWith("d")) {
				return digits + RID_ENDS_WITH_D;
			} else if (rsnLC.endsWith("s")) {
				return digits + RID_ENDS_WITH_S;
			}
			System.out.printf("\nUnexptected route ID for %s!\n", gRoute);
			System.exit(-1);
			return -1l;
		}
		return super.getRouteId(gRoute);
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
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
			System.out.printf("\nUnexptected route long name for %s!\n", gRoute);
			System.exit(-1);
			return null;
		}
		return super.getRouteLongName(gRoute);
	}

	private static final String AGENCY_COLOR_GREEN = "006F3C"; // GREEN (from web site CSS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public String getRouteColor(GRoute gRoute) {
		if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			int rsn = Integer.parseInt(gRoute.getRouteShortName());
			switch (rsn) {
			// @formatter:off
			case 101: return "6345A6";
			case 103: return "F2C61D";
			case 107: return "4866BA";
			case 110: return "231F20";
			case 111: return "6AA5D9";
			case 112: return "EE2428";
			case 120: return "CC8ACB";
			case 181: return "A9A9A9"; // ???
			case 182: return "A9A9A9"; // ???
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
			case 407: return "AAA8A9";
			case 409: return "4159B1";
			case 410: return "A14A94";
			case 411: return "536DBE";
			case 412: return "85D168";
			case 414: return "CE92CF";
			case 416: return "EC5788";
			case 417: return "74459B";
			case 501: return "2D4CA3";
			case 502: return "5FC3F0";
			case 506: return "F14823";
			case 601: return "2D4CA3";
			case 701: return null; // TODO ???
			case 801: return "4EA3DD";
			case 890: return null; // TODO ???
			case 900: return "DE581E";
			case 910: return "75AC95";
			case 915: return "36612B";
			case 916: return "5D1B1E";
			case 922: return "231F20";
			case 923: return "65A930";
			case 950: return "87CF25";
			case 960: return "B2D235";
			// @formatter:on
			}
		}
		if ("101A".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "6345A6"; // 101
		} else if ("103B".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "A7516D";
		} else if ("110A".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "231F20"; // 110
		} else if ("110B".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "7ACD5A";
		} else if ("110sh".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "231F20"; // 110 // TODO ?
		} else if ("111A".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "6AA5D9"; // 111
		} else if ("111S".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "6AA5D9"; // 111 // TODO ?
		} else if ("112CS".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "EE2428"; // 112 // TODO ?
		} else if ("112SH".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "EE2428"; // 112 // TODO ?
		} else if ("193A".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return null; // TODO ???
		} else if ("193B".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return null; // TODO ???
		} else if ("218D".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "988FD0"; // 218
		} else if ("219C".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "4D71C1"; // 219
		} else if ("219D".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "4D71C1"; // 219
		} else if ("219DSH".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "4D71C1"; // 219 // TODO ?
		} else if ("221D".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "F3B722"; // 221
		} else if ("223C".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "602C83"; // 223
		} else if ("224D".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "64C430"; // 224
		} else if ("224DSH".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "64C430"; // 224 // TODO ?
		} else if ("225A".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "9A8D7D"; // 225
		} else if ("226S".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "6345A6"; // 226 // TODO ?
		} else if ("232S".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "EE2428"; // 232 // TODO ?
		} else if ("308C".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "AC57B2"; // 308
		} else if ("318SH".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "9BA821"; // 318 // TODO ?
		} else if ("401B".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "8AD9F2";
		} else if ("401C".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "6C57B1";
		} else if ("402B".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "9BA821";
		} else if ("407S".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "AAA8A9"; // 407 // TODO ?
		} else if ("407C".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "AAA8A9"; // 407 // TODO ?
		} else if ("407CS".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "AAA8A9"; // 407 // TODO ?
		} else if ("410S".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "A14A94"; // 410 // TODO ?
		} else if ("411S".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "536DBE"; // 411 // TODO ?
		} else if ("412S".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "85D168"; // 412 // TODO ?
		} else if ("412CS".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "85D168"; // 412 // TODO ?
		} else if ("910C".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "9BA821";
		} else if ("922B".equalsIgnoreCase(gRoute.getRouteShortName())) {
			return "3DA87F";
		}
		if (isGoodEnoughAccepted()) {
			return null;
		}
		System.out.printf("\nUnexpected route color %s!\n", gRoute);
		System.exit(-1);
		return null;
	}

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		map2.put(101L, new RouteTripSpec(101L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Pickering Sta", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Montgomery Pk Rd") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Mont Broc:1", // MONTGOMERY PARK EASTBOUND @ BROCK RD
								"Bayl Broc2:1", // BAYLY WESTBOUND @ BROCK RD
								"Pick GO1:1", // PICKERING STATION
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Pick GO1:1", // PICKERING STATION
								"Live Annl1:1", // ++
								"Mont Broc:1", // MONTGOMERY PARK EASTBOUND @ BROCK RD
						})) //
				.compileBothTripSort());
		map2.put(107L, new RouteTripSpec(107L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Rougemount", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Pickering Sta") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Pick GO1:1", // "2549", // Pickering Station
								"Okla Hillv:1", // ++
								"Rose Roug:1", // "1634", // Rosebank Southbound @ Rougemount
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Rose Roug:1", // "1634", // Rosebank Southbound @ Rougemount
								"Okla Hill:1", // ++
								"West Cecy2:1", // ==
								"West Bayl1:1", // !=
								"Bayl West2:1", // ==
								"Pick GO1:1", // "2549", // Pickering Station
						})) //
				.compileBothTripSort());
		map2.put(110L + RID_ENDS_WITH_B, new RouteTripSpec(110L + RID_ENDS_WITH_B, // 110B
				0, MTrip.HEADSIGN_TYPE_STRING, "Kingston & Whites", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Pickering Pkwy Terminal") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Pick Term1:1", // "93112", // Pickering Parkway Terminal
								"Finc Whis:1", // ++
								"Shep Edmu2:1", // !=
								"Shep Whit2:1", // <> "1882", // Sheppard Eastbound @ Whites
								"Whit Shep2:1", // <>
								"Whit King1:1", // <> "93216", // Whites Southbound @ Kingston
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Shep Whit2:1", // <> "1882", // Sheppard Eastbound @ Whites
								"Whit Shep2:1", // <>
								"Whit King1:1", // <> "93216", // Whites Southbound @ Kingston
								"King Whit2:1", // !=
								"Finc Erin2:1", // ++
								"Pick Term1:1", // "93112", // Pickering Parkway Terminal
						})) //
				.compileBothTripSort());
		map2.put(112L, new RouteTripSpec(112L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Pickering Sta", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Zents") // William Jackson Dr
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Till Scen:1", // TILLINGS NORTHBOUND @ SCENIC
								"Zent Broc:1", // ZENTS EASTBOUND @ BROCK RD
								"Pick GO1:1", // PICKERING STATION
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Pick GO1:1", // PICKERING STATION
								"Will Broc_1:1", // WILLIAM JACKSON @ BROCK RD
								"Till Scen:1", // TILLINGS NORTHBOUND @ SCENIC
						})) //
				.compileBothTripSort());
		map2.put(216L, new RouteTripSpec(216L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Taunton") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Taun Sale2:1", // "2406", // Taunton Westbound @ Salem
								"Harw Ring1:1", // ++
								"Ajax Go8:5", // "2569", // Ajax Station
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Ajax Go8:5", // "2569", // Ajax Station
								"Harw Taun2:1", // "93140", // Harwood Northbound @ Taunton
								"Taun Sale1:1", // "2400", // Taunton Eastbound @ Salem
						})) //
				.compileBothTripSort());
		map2.put(217L, new RouteTripSpec(217L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Dreyer & Pickering Beach", // Pickering Beach
				1, MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Ajax Go8:5", // "2569", // Ajax Station
								"Clem Mona:1", // ++
								"Pick Drey2:1", // "2084", // Pickering Beach Southbound @ Dreyer
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Pick Beac:1", // "3174", // Pickering Beach Northbound @ Beachview
								"Finl Chri:1", // ++
								"Ajax Go8:5", // "2569", // Ajax Station
						})) //
				.compileBothTripSort());
		map2.put(218L, new RouteTripSpec(218L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Harwood") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Drey Park:1", // "2026", // DREYER EASTBOUND @ PARKER
								"Ajax Go8:5", // "2569", // AJAX STATION
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Ajax Go8:5", // "2569", // AJAX STATION
								"Drey Harw2:1", // "3089", // DREYER @ HARWOOD w ns
						})) //
				.compileBothTripSort());
		map2.put(218l + RID_ENDS_WITH_D, new RouteTripSpec(218l + RID_ENDS_WITH_D, // 218D
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Audley Rd") // Pickering Beach Rd
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Ashb Audl:1", "Bayl Port2:1", "Ajax Go8:5" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Ajax Go8:5", "King Parr:1", "Drey Pick:1", "Ashb Audl:1" })) //
				.compileBothTripSort());
		map2.put(219L, new RouteTripSpec(219L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Taunton", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Ajax Go8:5", // "2569", // Ajax Station
								"Rave Hyde1:1", // ++
								"Rave Ross1:1", // == "2186", // Ravenscroft Northbound @ Rossland
								"Ross Stev3:1", // !=
								"Segg Brad1", // != "", // Seggar Northbound @ Bradbury
								"Will West1:1", // != "93067", // Williamson Westbound @ Westney =>
								"Rave Dent:1", // !=
								"Taun Harw3:1", // != "2729", // Taunton Eastbound @ Harwood
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Taun Harw3:1", // "2729", // Taunton Eastbound @ Harwood
								"Will West1:1", // "93067", // Williamson Westbound @ Westney
								"Ajax Go8:5", // "2569", // Ajax Station
						})) //
				.compileBothTripSort());
		map2.put(219l + RID_ENDS_WITH_D, new RouteTripSpec(219l + RID_ENDS_WITH_D, // 219D
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "North Ajax", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Ajax Go8:5", "Rave Hyde1:1", "Will Segg:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Will Segg:1", "Chur Dela1:1", "Ajax Go8:5" })) //
				.compileBothTripSort());
		map2.put(221l, new RouteTripSpec(221l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Central Ajax Loop", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Ajax Go8:5", "Grif Turr:1", "Ross West1:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Ross West1:1", "Cole Chap:1", "Ajax Go8:5" })) //
				.compileBothTripSort());
		map2.put(221l + RID_ENDS_WITH_D, new RouteTripSpec(221l + RID_ENDS_WITH_D, // 221D
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Central Ajax Loop", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Ajax Go8:5", "Grif Turr:1", "Ross West1:1", "Cole Chap:1", "Harw Gard:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Harw Gard:1", "Mona Clem1:1", "Ajax Go8:5" })) //
				.compileBothTripSort());
		map2.put(222l, new RouteTripSpec(222l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Audley South") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Bayl Harw1:1", "Audl Upch:1", "Ajax Go8:5" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Ajax Go8:5", "Bayl Mack1:1", "Bayl Harw1:1" })) //
				.compileBothTripSort());
		map2.put(223L, new RouteTripSpec(223L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Glenanna", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Dreyer") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Pick Drey2:1", // "2084", // Pickering Beach Southbound @ Dreyer
								"Bayl Shoa2:1", // ++
								"Bayl Westn2:1", // !=
								"West Bayl4:1", // <>
								"West Obri2:1", // <>
								"Ajax Go8:5", // <> "2569", // Ajax Station
								"Fair West3:1", // <>
								"West Obri1:1", // !=
								"Glen Espl1:1", // "1915", // Glenanna Southbound @ The Esplanade (South side stop)
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Glen Espl1:1", // "1915", // Glenanna Southbound @ The Esplanade
								"Pick Term1:1", // "93112", // Pickering Parkway Terminal
								"Bayl Chur1:1", // !=
								"West Bayl4:1", // <>
								"West Obri2:1", // <>
								"Ajax Go8:5", // <> "2569", // Ajax Station
								"Fair West3:1", // <>
								"Bayl Westn1:1", // !=
								"Bayl Shoa1:1", // ++
								"Pick Beac:1", // "3174", // Pickering Beach Northbound @ Beachview
						})) //
				.compileBothTripSort());
		map2.put(224l, new RouteTripSpec(224l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Kerrison", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"Ajax Go8:5", // "2569", // AJAX STATION
								"PBea Bayl:1", // "3048", // PICKERING BEACH NORTHBOUND @ BAYLY
								"Keri Sale2:1", // "3028", // KERRISON EASTBOUND @ SALEM
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"Keri Sale2:1", // "3028", // KERRISON EASTBOUND @ SALEM
								"Sale King2:1", // "93151", // SALEM SOUTHBOUND @ KINGSTON
								"Sale Bayl2:1", // "3053", // SALEM SOUTHBOUND @ BAYLY
								"Ajax Go8:5", // "2569", // AJAX STATION
						})) //
				.compileBothTripSort());
		map2.put(224l + RID_ENDS_WITH_D, new RouteTripSpec(224l + RID_ENDS_WITH_D, // 224D
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Lk Driveway") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Lake Harw:1", "Finl Lake:1", "West Clem:1", "Ajax Go8:5" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Ajax Go8:5", "Harw West2:1", "Lake Harw:1" })) //
				.compileBothTripSort());
		map2.put(225l, new RouteTripSpec(225l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Taunton", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Ajax Go8:5", //
								"Taun Sale2:1", //
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Taun Sale1:1", //
								"Ajax Go8:5", //
						})) //
				.compileBothTripSort());
		map2.put(225l + RID_ENDS_WITH_A, new RouteTripSpec(225l + RID_ENDS_WITH_A, // 225A
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Taunton", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Ajax Go8:5", //
								"Adam Chad:1", //
								"Ross Harw1:1", //
								"Harw Will1:1", //
								"Taun Sale1:1", //
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Taun Sale1:1", //
								"Turn Kerr:1", //
								"Ajax Go8:5", //
						})) //
				.compileBothTripSort());
		map2.put(226l, new RouteTripSpec(226l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Harwood") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Clov Pitt:1", "Ajax Go8:5" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Ajax Go8:5", "Clov Pitt:1" })) //
				.compileBothTripSort());
		map2.put(232l, new RouteTripSpec(232l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Strickland", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Ajax Go8:5", // AJAX STATION
								"Chur Stri2:1", // CHURCH NORTHBOUND @ STRICKLAND
								"Ross Chur2:1", // ROSSLAND EASTBOUND @ 575 ROSSLAND
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Ross Chur2:1", // ROSSLAND EASTBOUND @ 575 ROSSLAND
								"Ajax Go8:5" // AJAX STATION
						})) //
				.compileBothTripSort());
		map2.put(301L, new RouteTripSpec(301L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Taunton", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Whit Go1:1", // Whitby Station
								"Mcqu Dund:1", // ++
								"Mcqu Bona:1", // ++ McQuay Northbound @ Bonacord
								"Coun Ross:1", // Country Lane Northbound @ Rossland
								"Taun Coun3:1", //
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Taun Coun3:1", //
								"Coch Ross1:1", // Cochrane Southbound @ Rossland
								"Bona Coch:1", // ++ Bonacord Westbound @ Cochrane
								"Whit Go1:1", // Whitby Station
						})) //
				.compileBothTripSort());
		map2.put(302L, new RouteTripSpec(302L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Brooklin", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Whit Go1:1", // "2576", // Whitby Station
								"Broc Dund1:1", // "171", // Brock Street Northbound @ Dundas
								"Bald Cass:1", // "492", // Baldwin Northbound @ Cassels
								"Carn Down:1", //
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Carn Down:1", // ==
								"Bald Nort:1", // != ??
								"Bald Way:1", // !=
								"Camp Bald:1", // <=>
								"Vipo Mont:1", // <=>
								"Vipo Ferg:1", // <=>
								"Vipo Dari:1", // !=
								"Winc Bald:1", // != ??
								"Carn Bald11", // != ??
								"Vipo Sabr:1", // !=
								"Vipo Ferg:1", // <=>
								"Vipo Mont:1", // <=>
								"Camp Bald:1", // <=>
								"Bald Winc11", // != ??
								"Bald Royb2:1", // ==
								"Bald Taun:1", // "196", // Baldwin Southbound @ Taunton
								"Whit Go1:1", // "2576", // Whitby Station
						})) //
				.compileBothTripSort());
		map2.put(303L, new RouteTripSpec(303L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Taunton", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Whit Go1:1", // Whitby Station
								"Gard Dund1:1", // ++
								"Gard Taun1:1", // ++ GARDEN NORTHBOUND @ TAUNTON
								"Mcki Broa2:1", // McKinney Southbound @ Broadleaf
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Mcki Broa2:1", // McKinney Southbound @ Broadleaf
								"Gard Taun4:1", // ++
								"Whit Go1:1", // Whitby Station
						})) //
				.compileBothTripSort());
		map2.put(304l, new RouteTripSpec(304l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Taunton", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Whit Go1:1", "Ande Ross1:1", "Ande Taun1:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Ande Taun1:1", "Lazi Sama:1", "Ande Taun2:1", "Ande John1:1", "Whit Go1:1" })) //
				.compileBothTripSort());
		map2.put(305l, new RouteTripSpec(305l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Taunton", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Whit Go1:1", // "2576", // WHITBY STATION
								"Thic Dund1:1", // ++
								"Thic Ross2:1", // ++
								"Taun Thic2:1", // "383", // TAUNTON EASTBOUND @ THICKSON
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Taun Thic2:1", // "383", // TAUNTON EASTBOUND @ THICKSON
								"Thic Craw2:1", // ++
								"Whit Go1:1", // "2576", // WHITBY STATION
						})) //
				.compileBothTripSort());
		map2.put(308l, new RouteTripSpec(308l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Sta", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ontario Shrs") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Gord Hosp:1", //
								"Gord Jame2:1", //
								"Scad Whit1:1", //
								"Whit Go1:1", //
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Whit Go1:1", //
								"Vict Gord2:1", //
								"Gord Hosp:1", //
						})) //
				.compileBothTripSort());
		map2.put(308l + RID_ENDS_WITH_C, new RouteTripSpec(308l + RID_ENDS_WITH_C, // 308C
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Sta", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ontario Shrs") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Gord Hosp:1", //
								"Whit Go1:1", //
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Whit Go1:1", //
								"Gord Hosp:1", //
						})) //
				.compileBothTripSort());
		map2.put(310L, new RouteTripSpec(310L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "UOIT / DC North Campus", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Brooklin") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Carn Bald:1", // "93482", // Carnwith Eastbound @ Baldwin
								"Thic Winc3:1", // "93487", // Thickson Southbound @ Winchester
								"Uoit Simc1:1", // "698", // UOIT / Durham College North Campus
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Uoit Simc1:1", // "698", // UOIT / Durham College North Campus
								"Winc Durh:1", // "523", // Winchester Westbound @ Durham
								"Carn Bald:1", // "93482", // Carnwith Eastbound @ Baldwin
						})) //
				.compileBothTripSort());
		map2.put(312l, new RouteTripSpec(312l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Oshawa Ctr Terminal", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"Coch Taun:1", //
								"Ande Craw2:1", //
								"Oc   Elmg3:1", // Oshawa Centre Terminal
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"Oc   Elmg3:1", // Oshawa Centre Terminal
								"Dund Bowm:1", //
								"Coch Taun:1", //
						})) //
				.compileBothTripSort());
		map2.put(318l, new RouteTripSpec(318l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Taunton", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Shrs") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Whit Go1:1", //
								"Gard Mead1:1", //
								"Gard Taun3:1", //
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Gard Taun3:1", //
								"Ross Coun2:1", //
								"Jeff Dund2:1",//
								"Whit Go1:1", //
						})) //
				.compileBothTripSort());
		map2.put(401L, new RouteTripSpec(401L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Lakeview Pk", //
				1, MTrip.HEADSIGN_TYPE_STRING, "UOIT / DC North Campus") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Uoit Simc1:1", // "698", // UOIT / Durham College North Campus
								"Cent King:1", // "659", // Centre Southbound @ King
								"Rits Went1:1", // "3112", // Ritson Southbound @ Wentworth
								"Simc Rits:1", // "1023", // Simcoe Southbound @ Ritson
								"Lake Rits:1", // "1029", // Lakeview Park Westbound @ Ritson
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Lake Rits:1", // "1029", // Lakeview Park Westbound @ Ritson
								"Ceda Went:1", // "989", // Cedar Northbound @ Wentworth
								"Simc Bond:1", // "677", // Simcoe Northbound @ Bond
								"Uoit Simc1:1", // "698", // UOIT / Durham College North Campus
						})) //
				.compileBothTripSort());
		map2.put(401L + RID_ENDS_WITH_B, new RouteTripSpec(401L + RID_ENDS_WITH_B, // 401B
				0, MTrip.HEADSIGN_TYPE_STRING, "King", //
				1, MTrip.HEADSIGN_TYPE_STRING, "UOIT / DC North Campus") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Uoit Simc1:1", // "698", // UOIT / Durham College North Campus
								"Cent King:1", // "659", // Centre Southbound @ King
								"Cent John:1", // "661", // Centre Southbound @ John
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Simc Elm:1", // "672", // Simcoe Northbound @ Elm St
								"Simc John1:1", // "674", // Simcoe Northbound @ John
								"Simc Bond:1", // "677", // Simcoe Northbound @ Bond
								"Uoit Simc1:1", // "698", // UOIT / Durham College North Campus
						})) //
				.compileBothTripSort());
		map2.put(401L + RID_ENDS_WITH_C, new RouteTripSpec(401L + RID_ENDS_WITH_C, // 401C
				0, MTrip.HEADSIGN_TYPE_STRING, "Oshawa Sta", //
				1, MTrip.HEADSIGN_TYPE_STRING, "UOIT / DC North Campus") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Uoit Simc1:1", // "698", // UOIT / Durham College North Campus
								"Simc Buck:1", // ++
								"Osha Go1:14", // "2585", // Oshawa Station
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Osha Go1:14", // "2585", // Oshawa Station
								"Simc Park2:1", // ++
								"Uoit Simc1:1", // "698", // UOIT / Durham College North Campus
						})) //
				.compileBothTripSort());
		map2.put(402L, new RouteTripSpec(402L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Courtice Rd", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Oshawa Ctr Terminal") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Oc   Elmg3:1", // "2595", // Oshawa Centre Terminal
								"King Warr:1", // ==
								"King Nass:1", // !=
								"King Midt:1", // ==
								"Hwy2 Trul2:1", // ==
								"Court Hwy21", // !=
								"Cour Moys1:1", // == "93323", // Courtice Northbound @ Moyse
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Cour Moys:1", // "1217", // Courtice Southbound @ Moyse (Courtice Community Complex)
								"Bond Clay1:1", // ++
								"Oc   Elmg3:1", // "2595", // Oshawa Centre Terminal
						})) //
				.compileBothTripSort());
		map2.put(403L, new RouteTripSpec(403L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Oshawa Sta", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Oshawa Ctr Terminal") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Oc   Elmg3:1", // "2595", // Oshawa Centre Terminal
								"Stev Phil2:1", // "940", // Stevenson Northbound @ Phillip Murray
								"Osha Go1:14", // "2585", // Oshawa Station
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Osha Go1:14", // "2585", // Oshawa Station
								"Stev Phil1:1", // "939", // Stevenson Southbound @ Phillip Murray
								"Oc   Elmg3:1", // "2595", // Oshawa Centre Terminal
						})) //
				.compileBothTripSort());
		map2.put(405L, new RouteTripSpec(405L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Oshawa Sta", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Harmony Terminal") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Harm Term:1", // "812", // Harmony Terminal
								"Elmg West1:1", // !=
								"Oc   Elmg3:1", // <> "2595", // Oshawa Centre Terminal
								"Stev Gibb3:1", // !=
								"Osha Go1:14", // "2585", // Oshawa Station
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Osha Go1:14", // "2585", // Oshawa Station
								"Gibb Stev3:1", // !=
								"Oc   Elmg3:1", // <> "2595", // Oshawa Centre Terminal
								"Elmg West2:1", // !=
								"Wils Bond:1", // "3120", // Wilson Northbound @ Bond {43.903539,-78.844486}
								"Harm Term:1", // "812", // Harmony Terminal
						})) //
				.compileBothTripSort());
		map2.put(407L, new RouteTripSpec(407L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Harmony Terminal", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Colonel Sam") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Colo Fare2:1", // "90014", // Eastbound @ 1908 Colonel Sam
								"Fare Rale1:1", // "3114", // Farewell Northbound @ Raleigh
								"Rits Ormo2:1", // ==
								"Rits Lupl21", // !=
								"Rits Cold3:1", // ==
								"Harm Term:1", // "812", // Harmony Terminal
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Harm Term:1", // "812", // Harmony Terminal
								"Rits Taun1:1", // "861", // Ritson Southbound @ Taunton
								"Bond Rits:1", // "1163", // Bond Westbound @ Ritson
								"Fare Rale:1", // "1119", // Farewell Southbound @ Raleigh
								"Colo Fare2:1", // "90014", // Eastbound @ 1908 Colonel Sam
						})) //
				.compileBothTripSort());
		map2.put(408L, new RouteTripSpec(408L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Oshawa Ctr Terminal", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Taunton") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Taun Thic2:1", // "383", // Taunton Eastbound @ Thickson
								"Gibb Stev2:1", // ==
								"Gibb Stev3:1", // !=
								"Oc   Elmg3:1", // == "2595", // Oshawa Centre Terminal
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Oc   Elmg3:1", // "2595", // Oshawa Centre Terminal
								"Garr Fore:1", // ++
								"Taun Thic2:1", // "383", // Taunton Eastbound @ Thickson
						})) //
				.compileBothTripSort());
		map2.put(409L, new RouteTripSpec(409L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Taunton", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Oshawa Ctr Terminal") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Oc   Elmg3:1", // "2595", // Oshawa Centre Terminal
								"Garr Fore:1", // ++
								"Taun Thic2:1", // "383", // Taunton Eastbound @ Thickson
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Taun Thic2:1", // "383", // Taunton Eastbound @ Thickson
								"Stev Cart:1", // ==
								"Gibb Stev3:1", // !=
								"Oc   Elmg3:1", // == "2595", // Oshawa Centre Terminal
						})) //
				.compileBothTripSort());
		map2.put(411L, new RouteTripSpec(411L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Courtice Rd", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Oshawa Ctr Terminal") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Oc   Elmg3:1", // "2595", // Oshawa Centre Terminal
								"Oliv Keew2:1", // ++
								"Cour Moys:1", // "1217", // Courtice Southbound @ Moyse (Courtice Community Complex)
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Cour Moys:1", // "1217", // Courtice Southbound @ Moyse (Courtice Community Complex)
								"Glen Town:1", // "93445", // Glenabbey Westbound @ Townline
								"Oc   Elmg3:1", // "2595", // Oshawa Centre Terminal
						})) //
				.compileBothTripSort());
		map2.put(412L, new RouteTripSpec(412L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Courtice Rd", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Oshawa Ctr Terminal") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Oc   Elmg3:1", // "2595", // Oshawa Centre Terminal
								"Marg Town:1", // "736", // Margate Eastbound @ Townline
								"Cour Moys:1", // "1217", // Courtice Southbound @ Moyse (Courtice Community Complex)
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Cour Moys1:1", // "93323", // Courtice Northbound @ Moyse
								"Nash Town1:1", // "93337", // Nash Westbound @ Townline
								"Oc   Elmg3:1", // "2595", // Oshawa Centre Terminal
						})) //
				.compileBothTripSort());
		map2.put(414L, new RouteTripSpec(414L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Nonquon", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Oshawa Ctr Terminal") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Dean Norm2:1", // Dean Westbound @ Normandy
								"Oc   Elmg3:1", // Oshawa Centre Terminal
								"Nonq Mary:1", // ++
								"Ormond D:1", //
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Ormond D:1", //
								"Hill Adel:1", // ++
								"Oc   Elmg3:1", // Oshawa Centre Terminal
								"Dean Norm1:1", // Dean Eastbound @ Normandy
						})) //
				.compileBothTripSort());
		map2.put(420L, new RouteTripSpec(420L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Britannia Ave & Kentucky Derby", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "UOIT / DC North Campus") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Uoit Simc1:1", // "698", // UOIT / Durham College North Campus
								"Simc Conl:1", // ==
								"Simc Nort:1", // !=
								"Conl Walr1:1", // !=
								"Brit Simc1", // !=
								"Brit Thor1", // ==
								"Brit Kent1", // "3366", // Britannia Westbound @ Kentucky Derby
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Brit Kent1", // "3366", // Britannia Westbound @ Kentucky Derby
								"BBrit Thor21", // ==
								"Brit Simc21", // !=
								"Simc Conl21", // !=
								"Simc Foun:1", // ==
								"Uoit Simc1:1", // "698", // UOIT / Durham College North Campus
						})) //
				.compileBothTripSort());
		map2.put(501L, new RouteTripSpec(501L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Liberty", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Bowmanville P&R") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"Hwy2 Bosw1:1", "Chur Temp1:1" //
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"Chur Temp1:1", "Hwy2 Bosw1:1" //
						})) //
				.compileBothTripSort());
		map2.put(502l, new RouteTripSpec(502l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Liberty", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Brookhill") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"Chur Temp1:1", // "1480", // CHURCH EASTBOUND @ TEMPERANCE
								"Simp King:1", // "1498", // SIMPSON NORTHBOUND @ KING
								"Mear Conc1:1", //
								"Long Libe:1", // "3173", // LONGWORTH WESTBOUND @ LIBERTY
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"Long Libe:1", // "3173", // LONGWORTH WESTBOUND @ LIBERTY
								"Chur Temp1:1", // "1480", // CHURCH EASTBOUND @ TEMPERANCE
						})) //
				.compileBothTripSort());
		map2.put(506l, new RouteTripSpec(506l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Bowmanville", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Orono") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"Main Wint:1", // "93533", // MAIN SOUTHBOUND @ WINTER
								"Nort Grad:1", //
								"Lake Wate:1", // "93478", // LAKEBREEZE EASTBOUND @ WATERVIEW
								"Bowm Prin:1", // "1450", // BOWMANVILLE PARK AND RIDE
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"Bowm Prin:1", // "1450", // BOWMANVILLE PARK AND RIDE
								"Libe Vict:1", //
								"Lake Wate:1", // "93478", // LAKEBREEZE EASTBOUND @ WATERVIEW
								"Pete Taun:1", //
								"Main Mill:1", //
								"Main Wint:1", // "93533", // MAIN SOUTHBOUND @ WINTER
						})) //
				.compileBothTripSort());
		map2.put(601L, new RouteTripSpec(601L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Beaverton", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Uxbridge") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Welw Toro:1", // "2416", // WELWOOD EASTBOUND @ 6 WELWOOD
								"Albe Jone1:1", // "3193", // Albert Southbound @ Jones
								"Hw12 RR15:1", // "93528", // HIGHWAY 12 @ REGIONAL ROAD 15 n fsmb
								"9Mil Lake:1", // 9 MILE @ LAKEVIEW MANNOR n ns
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"9Mil Lake:1", // "3190", // 9 MILE @ LAKEVIEW MANNOR n ns
								"Albe Rive:1", // "2530", // Albert Northbound @ River
								"Broc Main2:1", // ==
								"Toro Broc4:1", // "2422", // TORONTO NORTHBOUND @ BROCK ST => END
								"Toro Broc2:1", // != "2438", // TORONTO SOUTHBOUND @ BROCK ST
								"Toro Doug2:1", // ++
								"Welw Toro:1", // WELWOOD EASTBOUND @ 6 WELWOOD
						})) //
				.compileBothTripSort());
		map2.put(603L, new RouteTripSpec(603L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Uxbridge", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Pickering Pkwy Terminal") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Pick Term1:1", // "93112", // Pickering Parkway Terminal
								"Broc B4071", // ==
								"Broc Hwy721", // !=
								"Hwy7 Mowb21", // !=
								"Mowb Brou1", // !=
								"OldB Broc1", // ==
								"Welw Toro:1", // "2416", // Eastbound @ 6 Welwood
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Toro Broc1:1", // "2424", // Toronto Southbound @ Brock Street
								"Welw Toro:1", // "2416", // Eastbound @ 6 Welwood
								"OldB Broc21", // ==
								"Mowb Brou21", // !=
								"Hwy7 Mowb1", // !=
								"Broc Hwy71", // !=
								"Broc Hw4021", // ==
								"Pick Term1:1", // "93112", // Pickering Parkway Terminal
						})) //
				.compileBothTripSort());
		map2.put(653l, new RouteTripSpec(653l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Orillia", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Beaverton") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Simc Mara:1", // "2536", // SIMCOE ST SOUTHBOUND @ MARA
								"Dunl Colb1", // "3426", // DUNLOP SOUTHBOUND @ COLBORNE - ORILLIA SOLDIERS' MEMORIAL HOSPITAL

						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Dunl Colb1", // "3426", // DUNLOP SOUTHBOUND @ COLBORNE - ORILLIA SOLDIERS' MEMORIAL HOSPITAL
								"Simc John2:1", // "2537", // SIMCOE ST SOUTHBOUND @ JOHN
						})) //
				.compileBothTripSort());
		map2.put(654l, new RouteTripSpec(654l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Lindsay", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Cannington") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"Came Ann:1", // "2533", // CAMERON EASTBOUND @ ANN
								"Ange Kent1", // "3428", // ANGELINE NORTHBOUND @ KENT
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"Ange Kent21", // "3429", // ANGELINE SOUTHBOUND @ KENT
								"Came Ann1:1", // "93549", // CAMERON @ ANN w ns {44
						})) //
				.compileBothTripSort());
		map2.put(701l, new RouteTripSpec(701l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "East Uxbridge Loop", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "West Uxbridge Loop") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"Welw Toro:1", //
								"Toro Mill2:1", // ==
								"Reac East:1", // !=
								"Broc Main2:1", // !=
								"Toro Broc2:1", // ==
								"Welw Toro:1", //
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"Welw Toro:1", //
								"Toro Mill2:1", // ==
								"Toro Popl:1", // !=
								"Toro Broc1:1", // !=
								"Toro Broc2:1", // ==
								"Welw Toro:1", //
						})) //
				.compileBothTripSort());
		map2.put(801l, new RouteTripSpec(801l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"7A Scug2:1", "Simc Gree2:1", "Curt Wate1:1" //
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"Curt Wate1:1", "Simc Beec:1", "7A Scug2:1" //
						})) //
				.compileBothTripSort());
		map2.put(890l, new RouteTripSpec(890l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Pickering Pkwy Terminal", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Ajax Go8:5", "Pick Term1:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Pick Term1:1", "Ajax Go8:5" })) //
				.compileBothTripSort());
		map2.put(900L, new RouteTripSpec(900L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "UofT Scarborough", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Oshawa") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Mary Bond:1", // "93239", // Mary Northbound @ Bond
								"Dund Pine:1", // ==
								"Dund Atho:1", // !=
								"Dund Broc1:1", // ==
								"Elle Mili1", // "9473", // Ellesmere Westbound @ Military Trail
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Elle Mili11", // "7698", // Ellesmere Eastbound @ Military Trail
								"King Whit3:1", // "1779", // Kingston Eastbound @ Whites
								"Dund Broc2:1", // ==
								"Dund Perr:1", // !=
								"Dund Hick:1", // ==
								"Mary Bond:1", // "93239", // Mary Northbound @ Bond
						})) //
				.compileBothTripSort());
		map2.put(910l, new RouteTripSpec(910l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "UOIT / DC North Campus", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Whit Go1:1", //
								"Oc   Elmg3:1", //
								"Uoit Simc1:1", //
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Uoit Simc1:1", //
								"Thor Ross2:1", //
								"Oc   Elmg3:1", //
								"Whit Go1:1", //
						})) //
				.compileBothTripSort());
		map2.put(915L, new RouteTripSpec(915L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "UOIT / DC North Campus", // Harmony Terminal
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Ajax Go8:5", // "2569", // Ajax Station
								"Taun Sale1:1", // "2400", // Taunton Eastbound @ Salem
								"Taun Some2:1", // == !=
								"Simc Taun2:1", // != <>
								"Uoit Simc1:1", // != <>"698", // UOIT / Durham College North Campus
								"Comm Simc1:1", // != <>
								"Simc Cayu2:1", // != <>
								"Taunt Simc21",// == !=
								"Harm Term:1", // "812", // Harmony Terminal
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Harm Term:1", // "812", // Harmony Terminal
								"Taunt Mary1", // == !=
								"Simc Taun2:1", // != <>
								"Uoit Simc1:1", // != <> "698", // UOIT / Durham College North Campus
								"Comm Simc1:1", // != <>
								"Simc Cayu2:1", // != <>
								"Taunt Simc1", // == !=
								"Taun Some1:1", // ==
								"Taun Vall1:1", // "370", // Taunton Westbound @ Valleywood
								"Ajax Go8:5", // "2569", // Ajax Station
						})) //
				.compileBothTripSort());
		map2.put(922L, new RouteTripSpec(922L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Sta", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Harmony Terminal") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Harm Term:1", // "812", // Harmony Terminal
								"Nash Town:1", // "1201", // Nash Eastbound @ Townline
								"Osha Go1:14", // "2585", // Oshawa Station
								"Whit Go1:1", // "2576", // Whitby Station
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Whit Go1:1", // "2576", // Whitby Station
								"Osha Go1:14", // "2585", // Oshawa Station
								"Town Nash2:1", // "786", // Townline Northbound @ Nash
								"Harm Term:1", // "812", // Harmony Terminal
						})) //
				.compileBothTripSort());
		map2.put(930l, new RouteTripSpec(930l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Cannington", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Deer Creek") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Deer Cree1", "Came Coun:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Came Ann:1", "Deer Cree1" })) //
				.compileBothTripSort());
		map2.put(931l, new RouteTripSpec(931l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Beaverton", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Deer Creek") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Deer Cree1", "9Mil Lake:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "9Mil Lake:1", "Deer Cree1" })) //
				.compileBothTripSort());
		map2.put(950L, new RouteTripSpec(950L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Uxbridge", //
				1, MTrip.HEADSIGN_TYPE_STRING, "UOIT / DC North Campus") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"Uoit Simc1:1", // "698", // UOIT / Durham College North Campus
								"Scug High:1", // !=
								"Curt Wate2:1", // <> "2491", // Curts Eastbound @ Port Perry Terminal
								"Wate Curt:1", // !=
								"Toro Broc1:1", // "2424", // Toronto Southbound @ Brock Street
								"Welw Toro:1", // "2416", // Eastbound @ 6 Welwood
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"Welw Toro:1", // "2416", // Eastbound @ 6 Welwood
								"Toro Broc4:1", // "2422", // Toronto Northbound @ Brock
								"Quee Wate:1", // !=
								"Curt Wate2:1", // <> "2491", // Curts Eastbound @ Port Perry Terminal
								"Scug Simc1:1", // !=
								"Uoit Simc1:1", // "698", // UOIT / Durham College North Campus
						})) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public String cleanStopOriginalId(String gStopId) {
		gStopId = CleanUtils.cleanMergedID(gStopId);
		return gStopId;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), gTrip.getDirectionId());
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 101L + RID_ENDS_WITH_A) { // 101A
			if (Arrays.asList( //
					"A - Pickering Sta", //
					"Pickering Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 103L) {
			if (Arrays.asList( //
					"B - Rosebank", //
					"C - Rosebank", //
					"Rosebank", //
					"Rouge Hl Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Rouge Hl Sta", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"C - Pickering Sta", //
					"Pickering Pkwy Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering Pkwy Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 110L + RID_ENDS_WITH_A) { // 110A
			if (Arrays.asList( //
					"A - Pickering Pkwy Terminal", //
					"Pickering Pkwy Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering Pkwy Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 110L + RID_ENDS_WITH_B) { // 110B
			if (Arrays.asList( //
					"B - Pickering Pkwy Terminal", //
					"Pickering Pkwy Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering Pkwy Terminal", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"B - Kingston & Whites", //
					"Kingston & Whites" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Kingston & Whites", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 111L + RID_ENDS_WITH_A) { // 111A
			if (Arrays.asList( //
					"A - Pickering Pkwy Terminal", //
					"Pickering Pkwy Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering Pkwy Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 181L) {
			if (Arrays.asList( //
					"Pickering", //
					"West Pickering" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 193L + RID_ENDS_WITH_A) { // 193A
			if (Arrays.asList( //
					"A - Pickering Town Ctr", //
					"Pickering Town Ctr" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering Town Ctr", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 193L + RID_ENDS_WITH_B) { // 193B
			if (Arrays.asList( //
					"B - Pickering Town Ctr", //
					"Pickering Town Ctr" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering Town Ctr", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 215L) {
			if (Arrays.asList( //
					"Kingston Rd", //
					"Taunton" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Taunton", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 223L) {
			if (Arrays.asList( //
					"Ajax Sta", //
					"Dreyer" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Dreyer", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 223L + RID_ENDS_WITH_C) { // 223C
			if (Arrays.asList( //
					"C - Shoal Pt", //
					"Shoal Pt" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Shoal Pt", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"C - Ajax Sta", //
					"Ajax Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Ajax Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 226L + RID_ENDS_WITH_S) { // 226S
			if (Arrays.asList( //
					"Bayly & Burcher", //
					"Lk Driveway" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Bayly & Burcher", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 291L) {
			if (Arrays.asList( //
					"Clements", //
					"Pickering Town Ctr" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering Town Ctr", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 292l) {
			if (Arrays.asList( //
					"Clements", //
					"Pickering Town Ctr" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering Town Ctr", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 310L) {
			if (Arrays.asList( //
					"Brooklin", //
					"UOIT / DC North Campus" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("UOIT / DC North Campus", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 401L) {
			if (Arrays.asList( //
					"Cedar & Wentworth", //
					"King", //
					"UOIT / DC North Campus" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("UOIT / DC North Campus", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"King", //
					"Lakeview Pk" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Lakeview Pk", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 401L + RID_ENDS_WITH_B) { // 401B
			if (Arrays.asList( //
					"B - Ctr & John", //
					"UOIT / DC North Campus" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("UOIT / DC North Campus", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 403L) {
			if (Arrays.asList( //
					"S - Phillip Murray", //
					"Phillip Murray", //
					"Oshawa Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Oshawa Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 405l) {
			if (Arrays.asList( //
					"Harmony Terminal", //
					"Oshawa Ctr Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Harmony Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 407L) {
			if (Arrays.asList( //
					"S - Farewell & Raleigh", //
					"Colonel Sam" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Colonel Sam", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 407L + RID_ENDS_WITH_C) { // 407C
			if (Arrays.asList( //
					"C - Harmony Terminal", //
					"Harmony Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Harmony Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 410L) {
			if (Arrays.asList( //
					"Olive & Ritson", //
					"Oshawa Ctr Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Oshawa Ctr Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 412l) {
			if (Arrays.asList( //
					"Oshawa Ctr Terminal", //
					"Townline Rd" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Oshawa Ctr Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 915L) {
			if (Arrays.asList( //
					"S - Salem & Taunton", //
					"Salem", //
					"C - Harmony Terminal", //
					"Harmony Terminal", //
					"B - UOIT / DC North Campus", //
					"UOIT / DC North Campus" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("UOIT / DC North Campus", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"D - Ajax Sta", //
					"Ajax Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Ajax Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 916L) {
			if (Arrays.asList( //
					"C - Pickering Pkwy Terminal", //
					"Pickering Pkwy Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering Pkwy Terminal", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"C - Harmony Terminal", //
					"Harmony Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Harmony Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 922L) {
			if (Arrays.asList( //
					"B - Oshawa Sta", // ==
					"Oshawa Sta", // ==
					"Whitby Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Whitby Sta", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"B - Oshawa Sta", // ==
					"Oshawa Sta", // ==
					"Townline & Nash", //
					"B - Uxbridge", //
					"Uxbridge", //
					"Harmony Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Harmony Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 950L) {
			if (Arrays.asList( //
					StringUtils.EMPTY, //
					"Toronto & Brock (Uxbridge)", //
					"Welwood Dr (Uxbridge)", //
					"B - Uxbridge", //
					"Uxbridge" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Uxbridge", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"C - UOIT / DC North Campus", //
					"UOIT / DC North Campus" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("UOIT / DC North Campus", mTrip.getHeadsignId());
				return true;
			}
		}
		System.out.printf("\nUnexpected trips to merge: %s & %s!\n", mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	private static final Pattern TO = Pattern.compile("((^|\\W){1}(to)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final Pattern VIA = Pattern.compile("((^|\\W){1}(via)(\\W|$){1})", Pattern.CASE_INSENSITIVE);

	private static final Pattern START_WITH_RSN = Pattern.compile("(^[\\d]+[A-Z]? \\- )", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		Matcher matcherTO = TO.matcher(tripHeadsign);
		if (matcherTO.find()) {
			String gTripHeadsignAfterTO = tripHeadsign.substring(matcherTO.end());
			tripHeadsign = gTripHeadsignAfterTO;
		}
		Matcher matcherVIA = VIA.matcher(tripHeadsign);
		if (matcherVIA.find()) {
			String gTripHeadsignBeforeVIA = tripHeadsign.substring(0, matcherVIA.start());
			tripHeadsign = gTripHeadsignBeforeVIA;
		}
		tripHeadsign = START_WITH_RSN.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
		tripHeadsign = CleanUtils.CLEAN_AT.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern UOIT = Pattern.compile(String.format("((^|\\W){1}(%s)(\\W|$){1})", "uoit"), Pattern.CASE_INSENSITIVE);
	private static final String UOIT_REPLACEMENT = String.format("$2%s$4", "UOIT");
	private static final Pattern NORTHBOUND = Pattern.compile(String.format("((^|\\W){1}(%s)(\\W|$){1})", "northbound"), Pattern.CASE_INSENSITIVE);
	private static final String NORTHBOUND_REPLACEMENT = String.format("$2%s$4", "NB");
	private static final Pattern SOUTHBOUND = Pattern.compile(String.format("((^|\\W){1}(%s)(\\W|$){1})", "southbound"), Pattern.CASE_INSENSITIVE);
	private static final String SOUTHBOUND_REPLACEMENT = String.format("$2%s$4", "SB");
	private static final Pattern EASTBOUND = Pattern.compile(String.format("((^|\\W){1}(%s)(\\W|$){1})", "eastbound"), Pattern.CASE_INSENSITIVE);
	private static final String EASTBOUND_REPLACEMENT = String.format("$2%s$4", "EB");
	private static final Pattern WESTBOUND = Pattern.compile(String.format("((^|\\W){1}(%s)(\\W|$){1})", "westbound"), Pattern.CASE_INSENSITIVE);
	private static final String WESTBOUND_REPLACEMENT = String.format("$2%s$4", "WB");

	@Override
	public String cleanStopName(String gStopName) {
		if (Utils.isUppercaseOnly(gStopName, true, true)) {
			gStopName = gStopName.toLowerCase(Locale.ENGLISH);
		}
		gStopName = UOIT.matcher(gStopName).replaceAll(UOIT_REPLACEMENT);
		gStopName = NORTHBOUND.matcher(gStopName).replaceAll(NORTHBOUND_REPLACEMENT);
		gStopName = SOUTHBOUND.matcher(gStopName).replaceAll(SOUTHBOUND_REPLACEMENT);
		gStopName = EASTBOUND.matcher(gStopName).replaceAll(EASTBOUND_REPLACEMENT);
		gStopName = WESTBOUND.matcher(gStopName).replaceAll(WESTBOUND_REPLACEMENT);
		gStopName = CleanUtils.cleanSlashes(gStopName);
		gStopName = CleanUtils.removePoints(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(GStop gStop) {
		if (!StringUtils.isEmpty(gStop.getStopCode()) && Utils.isDigitsOnly(gStop.getStopCode())) {
			return Integer.parseInt(gStop.getStopCode()); // use stop code as stop ID
		}
		String stopId = gStop.getStopId();
		stopId = CleanUtils.cleanMergedID(stopId);
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
