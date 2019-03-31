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
		boolean isNext = "next_".equalsIgnoreCase(args[2]);
		if (isNext) {
			setupNext();
		}
		super.start(args);
		System.out.printf("\nGenerating DRT bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	private void setupNext() {
		ALL_ROUTE_TRIPS2.clear();
		ALL_ROUTE_TRIPS2.put(112L, new RouteTripSpec(112L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Pickering Sta", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Zents") // William Jackson Dr
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"93279:1", // "93279", // William Jackson Eastbound @ Brock Road
								"3368:1", // "3368", // Brock Road Southbound @ Dersan
								"2549:1", // "2549", // Pickering Station
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"2549:1", // "2549", // Pickering Station
								"1847:1", // ++ Brock Road Northbound @ Major Oaks
								"93279:1", // "93279", // William Jackson Eastbound @ Brock Road
						})) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2.put(501L, new RouteTripSpec(501L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Church & Temperance", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Highway 2") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"1436:1", // "1436", // Highway 2 Westbound @ Boswell
								"1480:1", // "1480", // Church Eastbound @ Temperance
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"1480:1", // "1480", // Church Eastbound @ Temperance
								"1436:1", // "1436", // Highway 2 Westbound @ Boswell
						})) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2.put(502L, new RouteTripSpec(502L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Longworth & Liberty", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Church & Temperance") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"1480:1", // "1480", // Church Eastbound @ Temperance
								"1498:1", // "1498", // Simpson Northbound @ King
								"3173:1", // "3173", // Longworth Westbound @ Liberty
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"3173:1", // "3173", // Longworth Westbound @ Liberty
								"1480:1", // "1480", // Church Eastbound @ Temperance
						})) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2.put(653L, new RouteTripSpec(653L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Orillia", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Port Perry") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"2491:1", // "2491", // Curts Eastbound @ Port Perry Terminal
								"3426:1", // "3426", // Dunlop Southbound @ Colborne (Soldiers Memorial Hospital)
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"3426:1", // "3426", // Dunlop Southbound @ Colborne (Soldiers Memorial Hospital)
								"2491:1", // "2491", // Curts Eastbound @ Port Perry Terminal
						})) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2.put(654L, new RouteTripSpec(654L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Lindsay", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Port Perry") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"2491:1", // "2491", // Curts Eastbound @ Port Perry Terminal
								"3428:1", // "3428", // Angeline Northbound @ Kent (Ross Memorial Hospital)
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"3429:1", // "3429", // Angeline Southbound @ Kent (Ross Memorial Hospital)
								"2491:1", // "2491", // Curts Eastbound @ Port Perry Terminal
						})) //
				.compileBothTripSort());
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
								"Taun Coch1:1", // Taunton Eastbound @ Cochrane
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Taun Coch1:1", // Taunton Eastbound @ Cochrane
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
								"Gard Dund2:1", // ++
								"Gard Taun1:1", // ++ GARDEN NORTHBOUND @ TAUNTON
								"Gard Taun3:1", // Garden Northbound @ Taunton (North side stop)
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
						Arrays.asList(new String[] { //
						"Whit Go1:1", //
								"Ande Ross1:1", //
								"Ande Taun1:1", //
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Ande Taun1:1", //
								"Lazi Sama:1", //
								"Ande Taun2:1", //
								"Ande John1:1", //
								"Whit Go1:1", //
						})) //
				.compileBothTripSort());
		map2.put(308L, new RouteTripSpec(308L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Sta", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ontario Shrs") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Gord Hosp:1", // "58", // Ontario Shores (West Entrance)
								// "Gord Jame2:1", // ++
								"Onta Gord1:1", // ==
								"Scad Gord1:1", // !=
								"Vict Gord3:1", // !=
								"Gord Scad3:1", // !=
								"Gord Whit:1", // !=
								"Vict Gord1:1", // ==
								"Whit Go1:1", // "2576", // Whitby Station
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Whit Go1:1", // "2576", // Whitby Station
								"Vict Henr2:1", // ==
								"Vict Gord2:1", // != "90007", // Victoria Westbound @ Gordon (Abilities Centre)
								"Scad Gord:1", // !=
								"Gord WSG1", // !=
								"Gord 3001", // !=
								"Gord Scad1:1", // ==
								"Gord Hosp:1", // "58", // Ontario Shores (West Entrance)
						})) //
				.compileBothTripSort());
		map2.put(312L, new RouteTripSpec(312L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Oshawa Ctr Terminal", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Taunton") // "Whitby") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"Taun Broc:1", // Taunton Eastbound @ Brock Street
								"Ande Craw2:1", //
								"Oc   Elmg3:1", // Oshawa Centre Terminal
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"Oc   Elmg3:1", // Oshawa Centre Terminal
								"Dund Bowm:1", //
								"Coch Taun:1", //
								"Taun Broc:1", // Taunton Eastbound @ Brock Street
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
								"Mary Ormo:1", // "93531", // Mary Northbound @ Ormond
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Mary Ormo:1", // "93531", // Mary Northbound @ Ormond
								"Ormond D:1", // ++
								"Hill Adel:1", // ++
								"Oc   Elmg3:1", // Oshawa Centre Terminal
								"Dean Norm1:1", // Dean Eastbound @ Normandy
						})) //
				.compileBothTripSort());
		map2.put(501L, new RouteTripSpec(501L, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Liberty", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Bowmanville P&R") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"Hwy2 Bosw1:1", //
								"Chur Temp1:1", //
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"Chur Temp1:1", //
								"Hwy2 Bosw1:1", //
						})) //
				.compileBothTripSort());
		map2.put(502L, new RouteTripSpec(502L, //
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
		map2.put(653L, new RouteTripSpec(653L, //
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
		map2.put(654L, new RouteTripSpec(654L, //
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

	private static final String A__ = "A - ";
	private static final String B__ = "B - ";
	private static final String C__ = "C - ";
	private static final String D__ = "D - ";
	private static final String S__ = "S - ";

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 101L + RID_ENDS_WITH_A) { // 101A
			if (Arrays.asList( //
					A__ + "Pickering Sta", //
					"Pickering Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 103L) {
			if (Arrays.asList( //
					B__ + "Rosebank", //
					C__ + "Rosebank", //
					"Rosebank", //
					"Rouge Hl Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Rouge Hl Sta", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					C__ + "Pickering Sta", //
					"Pickering Pkwy Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering Pkwy Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 110L + RID_ENDS_WITH_A) { // 110A
			if (Arrays.asList( //
					A__ + "Pickering Pkwy Terminal", //
					"Pickering Pkwy Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering Pkwy Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 110L + RID_ENDS_WITH_B) { // 110B
			if (Arrays.asList( //
					B__ + "Pickering Pkwy Terminal", //
					"Pickering Pkwy Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering Pkwy Terminal", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					B__ + "Kingston & Whites", //
					"Kingston & Whites" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Kingston & Whites", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 111L + RID_ENDS_WITH_A) { // 111A
			if (Arrays.asList( //
					A__ + "Pickering Pkwy Terminal", //
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
					A__ + "Pickering Town Ctr", //
					"Pickering Town Ctr" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering Town Ctr", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 193L + RID_ENDS_WITH_B) { // 193B
			if (Arrays.asList( //
					B__ + "Pickering Town Ctr", //
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
		} else if (mTrip.getRouteId() == 216L) {
			if (Arrays.asList( //
					S__ + "Harwood & Taunton", //
					"Harwood & Taunton", //
					"Taunton" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Taunton", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 219L) {
			if (Arrays.asList( //
					C__ + "Williamson", //
					A__ + "Taunton", //
					"Taunton" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Taunton", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 223L) {
			if (Arrays.asList( //
					B__ + "Ajax Sta", //
					C__ + "Shoal Pt", //
					"Ajax Sta", //
					"Dreyer" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Dreyer", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					S__ + "Ajax Sta", //
					C__ + "Ajax Sta", //
					"Glenanna" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Glenanna", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 223L + RID_ENDS_WITH_C) { // 223C
			if (Arrays.asList( //
					C__ + "Shoal Pt", //
					"Shoal Pt" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Shoal Pt", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					C__ + "Ajax Sta", //
					"Ajax Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Ajax Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 224L) {
			if (Arrays.asList( //
					A__ + "Taunton", //
					B__ + "Kerrison", //
					S__ + "Kingston Rd & Salem", //
					"Taunton" // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Taunton", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					B__ + "Salem & Bayly", //
					"Kerrison" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Kerrison", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 225L) {
			if (Arrays.asList( //
					S__ + "Rushworth", //
					"Taunton" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Taunton", mTrip.getHeadsignId());
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
		} else if (mTrip.getRouteId() == 232L) {
			if (Arrays.asList( //
					"Ajax Sta", // <>
					"Rossland" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Rossland", mTrip.getHeadsignId());
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
		} else if (mTrip.getRouteId() == 301L) {
			if (Arrays.asList( //
					S__ + "Rossland & Cochrane", //
					"Whitby Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Whitby Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 302L) {
			if (Arrays.asList( //
					S__ + "Taunton", //
					"Whitby Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Whitby Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 308L) {
			if (Arrays.asList( //
					C__ + "Whitby Sta", //
					"Whitby Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Whitby Sta", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					C__ + "Ontario Shrs", //
					"Ontario Shrs" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Ontario Shrs", mTrip.getHeadsignId());
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
					B__ + "Wentworth", //
					C__ + "Oshawa Sta", //
					S__ + "Ctr & John", //
					S__ + "Ritson & Wentworth", //
					S__ + "Wentworth", //
					"Lakeview Pk" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Lakeview Pk", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 401L + RID_ENDS_WITH_B) { // 401B
			if (Arrays.asList( //
					"UOIT / DC North Campus" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("UOIT / DC North Campus", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					B__ + "Wentworth", //
					S__ + "Ctr & John", //
					"Lakeview Pk" // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Lakeview Pk", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 403L) {
			if (Arrays.asList( //
					S__ + "Phillip Murray", //
					"Phillip Murray", //
					"Oshawa Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Oshawa Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 405L) {
			if (Arrays.asList( //
					B__ + "Oshawa Ctr Terminal", //
					"Oshawa Ctr Terminal", //
					S__ + "Wilson & Bond", //
					"Harmony Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Harmony Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 407L) {
			if (Arrays.asList( //
					S__ + "Farewell & Raleigh", //
					"Colonel Sam" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Colonel Sam", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					C__ + "Harmony Terminal", //
					"Harmony Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Harmony Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 407L + RID_ENDS_WITH_C) { // 407C
			if (Arrays.asList( //
					C__ + "Harmony Terminal", //
					"Harmony Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Harmony Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 409L) {
			if (Arrays.asList( //
					A__ + "Oshawa Ctr Terminal", //
					B__ + "Oshawa Ctr Terminal", //
					"Oshawa Ctr Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Oshawa Ctr Terminal", mTrip.getHeadsignId());
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
		} else if (mTrip.getRouteId() == 412L) {
			if (Arrays.asList( //
					B__ + "Townline Rd", //
					"Townline Rd", //
					"Oshawa Ctr Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Oshawa Ctr Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 414L) {
			if (Arrays.asList( //
					"Dean & Normandy", //
					"Oshawa Ctr Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Oshawa Ctr Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 422L) {
			if (Arrays.asList( //
					S__ + "Ctr & King", //
					"Oshawa Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Oshawa Sta", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Simcoe & Conlin", //
					"Simcoe & Windfields Farm Dr" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Simcoe & Windfields Farm Dr", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 506L) {
			if (Arrays.asList( //
					B__ + "Newcastle", //
					"Orono" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Orono", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 603L) {
			if (Arrays.asList( //
					"Uxbridge", //
					"Port Perry" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Port Perry", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 601L) {
			if (Arrays.asList( //
					"Uxbridge", // <>
					B__ + "Sunderland", //
					"Beaverton" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Beaverton", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					B__ + "Uxbridge", //
					"Uxbridge" // <>
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Uxbridge", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 900L) {
			if (Arrays.asList( //
					B__ + "Kingston & Whites", //
					"UofT Scarborough" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("UofT Scarborough", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					B__ + "Glenanna", //
					"Scarborough" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Scarborough", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 915L) {
			if (Arrays.asList( //
					S__ + "Salem & Taunton", //
					"Salem", //
					C__ + "Harmony Terminal", //
					"Harmony Terminal", //
					B__ + "UOIT / DC North Campus", //
					"UOIT / DC North Campus" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("UOIT / DC North Campus", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					D__ + "Ajax Sta", //
					"Ajax Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Ajax Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 916L) {
			if (Arrays.asList( //
					C__ + "Pickering Pkwy Terminal", //
					"Pickering Pkwy Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Pickering Pkwy Terminal", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					C__ + "Harmony Terminal", //
					"Harmony Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Harmony Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 922L) {
			if (Arrays.asList( //
					B__ + "Oshawa Sta", // ==
					S__ + "Oshawa Sta", //
					"Oshawa Sta", // ==
					"Whitby Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Whitby Sta", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					B__ + "Oshawa Sta", // ==
					S__ + "Oshawa Sta", //
					"Oshawa Sta", // ==
					"Townline & Nash", //
					B__ + "Uxbridge", //
					"Uxbridge", //
					"Harmony Terminal" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Harmony Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 950L) {
			if (Arrays.asList( //
					StringUtils.EMPTY, //
					S__ + "Port Perry", //
					"Toronto & Brock (Uxbridge)", //
					S__ + "Uxbridge (Toronto & Brock)", //
					"Welwood Dr (Uxbridge)", //
					C__ + "Uxbridge (Welwood)", //
					D__ + "Uxbridge (Welwood)", //
					B__ + "Uxbridge", //
					"Uxbridge" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Uxbridge", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					C__ + "UOIT / DC North Campus", //
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

	private static final Pattern LATE_NIGHT_SHUTTLE_ = Pattern.compile("((^|\\W){1}(late night shuttle)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String LATE_NIGHT_SHUTTLE_REPLACEMENT = "$2$4";

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
		tripHeadsign = LATE_NIGHT_SHUTTLE_.matcher(tripHeadsign).replaceAll(LATE_NIGHT_SHUTTLE_REPLACEMENT);
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
