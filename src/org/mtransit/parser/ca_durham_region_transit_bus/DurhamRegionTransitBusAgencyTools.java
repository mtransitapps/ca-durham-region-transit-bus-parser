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

	private static final long RID_ENDS_WITH_CS = 3080000l;
	private static final long RID_ENDS_WITH_SH = 19080000l;

	private static final long RID_ENDS_WITH_A = 10000l;
	private static final long RID_ENDS_WITH_B = 20000l;
	private static final long RID_ENDS_WITH_C = 30000l;
	private static final long RID_ENDS_WITH_D = 40000l;
	private static final long RID_ENDS_WITH_S = 190000l;

	@Override
	public long getRouteId(GRoute gRoute) {
		if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			return Long.parseLong(gRoute.getRouteShortName());
		}
		if (!Utils.isDigitsOnly(gRoute.getRouteId())) {
			Matcher matcher = DIGITS.matcher(gRoute.getRouteId());
			if (matcher.find()) {
				int digits = Integer.parseInt(matcher.group());
				String routeIdLC = gRoute.getRouteId().toLowerCase(Locale.ENGLISH);
				if (routeIdLC.endsWith("cs")) {
					return digits + RID_ENDS_WITH_CS;
				} else if (routeIdLC.endsWith("sh")) {
					return digits + RID_ENDS_WITH_SH;
				}
				if (routeIdLC.endsWith("a")) {
					return digits + RID_ENDS_WITH_A;
				} else if (routeIdLC.endsWith("b")) {
					return digits + RID_ENDS_WITH_B;
				} else if (routeIdLC.endsWith("c")) {
					return digits + RID_ENDS_WITH_C;
				} else if (routeIdLC.endsWith("d")) {
					return digits + RID_ENDS_WITH_D;
				} else if (routeIdLC.endsWith("s")) {
					return digits + RID_ENDS_WITH_S;
				}
				System.out.printf("\nUnexptected route ID for %s!\n", gRoute);
				System.exit(-1);
				return -1l;
			}
		}
		return super.getRouteId(gRoute);
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteLongName())) {
			if ("101A".equalsIgnoreCase(gRoute.getRouteId())) {
				return "Industrial Bay Ridges";
			} else if ("219C".equalsIgnoreCase(gRoute.getRouteId())) {
				return "Ravenscroft";
			} else if ("232".equalsIgnoreCase(gRoute.getRouteId())) {
				return "Church";
			} else if ("308C".equalsIgnoreCase(gRoute.getRouteId())) {
				return "Whitby Shores";
			} else if ("310".equalsIgnoreCase(gRoute.getRouteId())) {
				return "Brooklin / UOIT & DC";
			} else if ("401C".equalsIgnoreCase(gRoute.getRouteId())) {
				return "Simcoe";
			} else if ("417".equalsIgnoreCase(gRoute.getRouteId())) {
				return "Conlin";
			} else if ("506".equalsIgnoreCase(gRoute.getRouteId())) {
				return "Wilmot Crk Orono Newcastle"; // Community Bus
			} else if ("910C".equalsIgnoreCase(gRoute.getRouteId())) {
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
			case 291: return null; // TODO ???
			case 292: return null; // TODO ???
			case 301: return "8D3CA3";
			case 302: return "EE2428";
			case 303: return "6EC95F";
			case 304: return "79D0ED";
			case 305: return "8448a9";
			case 308: return "AC57B2";
			case 310: return "DFC463";
			case 312: return "AAA8A9"; 
			case 318: return "9BA821";
			case 380: return null; // TODO ???
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
		if ("101A".equalsIgnoreCase(gRoute.getRouteId())) {
			return "6345A6"; // 101
		} else if ("103B".equalsIgnoreCase(gRoute.getRouteId())) {
			return "A7516D";
		} else if ("110A".equalsIgnoreCase(gRoute.getRouteId())) {
			return "231F20"; // 110
		} else if ("110B".equalsIgnoreCase(gRoute.getRouteId())) {
			return "7ACD5A";
		} else if ("110sh".equalsIgnoreCase(gRoute.getRouteId())) {
			return "231F20"; // 110 // TODO ?
		} else if ("111A".equalsIgnoreCase(gRoute.getRouteId())) {
			return "6AA5D9"; // 111
		} else if ("111S".equalsIgnoreCase(gRoute.getRouteId())) {
			return "6AA5D9"; // 111 // TODO ?
		} else if ("112CS".equalsIgnoreCase(gRoute.getRouteId())) {
			return "EE2428"; // 112 // TODO ?
		} else if ("112SH".equalsIgnoreCase(gRoute.getRouteId())) {
			return "EE2428"; // 112 // TODO ?
		} else if ("193A".equalsIgnoreCase(gRoute.getRouteId())) {
			return null; // TODO ???
		} else if ("193B".equalsIgnoreCase(gRoute.getRouteId())) {
			return null; // TODO ???
		} else if ("218D".equalsIgnoreCase(gRoute.getRouteId())) {
			return "988FD0"; // 218
		} else if ("219C".equalsIgnoreCase(gRoute.getRouteId())) {
			return "4D71C1"; // 219
		} else if ("219D".equalsIgnoreCase(gRoute.getRouteId())) {
			return "4D71C1"; // 219
		} else if ("219DSH".equalsIgnoreCase(gRoute.getRouteId())) {
			return "4D71C1"; // 219 // TODO ?
		} else if ("221D".equalsIgnoreCase(gRoute.getRouteId())) {
			return "F3B722"; // 221
		} else if ("224D".equalsIgnoreCase(gRoute.getRouteId())) {
			return "64C430"; // 224
		} else if ("224DSH".equalsIgnoreCase(gRoute.getRouteId())) {
			return "64C430"; // 224 // TODO ?
		} else if ("225A".equalsIgnoreCase(gRoute.getRouteId())) {
			return "9A8D7D"; // 225
		} else if ("226S".equalsIgnoreCase(gRoute.getRouteId())) {
			return "6345A6"; // 226 // TODO ?
		} else if ("232S".equalsIgnoreCase(gRoute.getRouteId())) {
			return "EE2428"; // 232 // TODO ?
		} else if ("308C".equalsIgnoreCase(gRoute.getRouteId())) {
			return "AC57B2"; // 308
		} else if ("318SH".equalsIgnoreCase(gRoute.getRouteId())) {
			return "9BA821"; // 318 // TODO ?
		} else if ("401B".equalsIgnoreCase(gRoute.getRouteId())) {
			return "8AD9F2";
		} else if ("401C".equalsIgnoreCase(gRoute.getRouteId())) {
			return "6C57B1";
		} else if ("402B".equalsIgnoreCase(gRoute.getRouteId())) {
			return "9BA821";
		} else if ("407S".equalsIgnoreCase(gRoute.getRouteId())) {
			return "AAA8A9"; // 407 // TODO ?
		} else if ("407C".equalsIgnoreCase(gRoute.getRouteId())) {
			return "AAA8A9"; // 407 // TODO ?
		} else if ("407CS".equalsIgnoreCase(gRoute.getRouteId())) {
			return "AAA8A9"; // 407 // TODO ?
		} else if ("410S".equalsIgnoreCase(gRoute.getRouteId())) {
			return "A14A94"; // 410 // TODO ?
		} else if ("411S".equalsIgnoreCase(gRoute.getRouteId())) {
			return "536DBE"; // 411 // TODO ?
		} else if ("412S".equalsIgnoreCase(gRoute.getRouteId())) {
			return "85D168"; // 412 // TODO ?
		} else if ("412CS".equalsIgnoreCase(gRoute.getRouteId())) {
			return "85D168"; // 412 // TODO ?
		} else if ("910C".equalsIgnoreCase(gRoute.getRouteId())) {
			return "9BA821";
		} else if ("922B".equalsIgnoreCase(gRoute.getRouteId())) {
			return "3DA87F";
		}
		System.out.printf("\nUnexpected route color %s!\n", gRoute);
		System.exit(-1);
		return null;
	}

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		map2.put(218l, new RouteTripSpec(218l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Harwood") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Drey Park:1", "Ajax Go8:5" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Ajax Go8:5", "Drey Harw2:1" })) //
				.compileBothTripSort());
		map2.put(218l + RID_ENDS_WITH_D, new RouteTripSpec(218l + RID_ENDS_WITH_D, // 218D
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Audley Rd") // Pickering Beach Rd
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Ashb Audl:1", "Bayl Port2:1", "Ajax Go8:5" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Ajax Go8:5", "King Parr:1", "Drey Pick:1", "Ashb Audl:1" })) //
				.compileBothTripSort());
		map2.put(219l, new RouteTripSpec(219l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Taunton", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Ajax Go8:5", "Rave Hyde1:1", "Rave Ross1:1", "Taun Harw3:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Taun Harw3:1", "Ajax Go8:5" })) //
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
		map2.put(224l, new RouteTripSpec(224l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Kerrison", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "Ajax Go8:5", "PBea Bayl:1", "Keri Sale2:1" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "Keri Sale2:1", "Sale King2:1", "Sale Bayl2:1", "Ajax Go8:5" })) //
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
						Arrays.asList(new String[] { "Ajax Go8:5", "Taun Sale2:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Taun Sale1:1", "Ajax Go8:5" })) //
				.compileBothTripSort());
		map2.put(225l + RID_ENDS_WITH_A, new RouteTripSpec(225l + RID_ENDS_WITH_A, // 225A
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Taunton", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ajax Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Ajax Go8:5", "Adam Chad:1", "Ross Harw1:1", "Harw Will1:1", "Taun Sale1:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Taun Sale1:1", "Turn Kerr:1", "Ajax Go8:5" })) //
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
						Arrays.asList(new String[] { "Ajax Go8:5", "Ross Chur2:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Ross Chur2:1", "Ajax Go8:5" })) //
				.compileBothTripSort());
		map2.put(301l, new RouteTripSpec(301l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Taunton", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Whit Go1:1", "Mcqu Dund:1", "Mcqu Bona:1", "Taun Coun3:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Taun Coun3:1", "Bona Coch:1", "Whit Go1:1" })) //
				.compileBothTripSort());
		map2.put(302l, new RouteTripSpec(302l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Brooklin", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Whit Go1:1", "Bald Cass:1", "Carn Down:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Carn Down:1", "Bald Taun:1", "Whit Go1:1" })) //
				.compileBothTripSort());
		map2.put(303l, new RouteTripSpec(303l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Taunton", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Whit Go1:1", "Gard Dund1:1", "Gard Taun1:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Gard Taun1:1", "Gard Taun4:1", "Whit Go1:1" })) //
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
						Arrays.asList(new String[] { "Whit Go1:1", "Thic Dund1:1", "Thic Ross2:1", "Taun Tom2:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Taun Tom2:1", "Garr Ross1:1", "Whit Go1:1" })) //
				.compileBothTripSort());
		map2.put(308l, new RouteTripSpec(308l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Sta", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ontario Shrs") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Gord Hosp:1", "Gord Jame2:1", "Scad Whit1:1", "Whit Go1:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Whit Go1:1", "Vict Gord2:1", "Gord Hosp:1" })) //
				.compileBothTripSort());
		map2.put(308l + RID_ENDS_WITH_C, new RouteTripSpec(308l + RID_ENDS_WITH_C, // 308C
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Sta", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Ontario Shrs") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Gord Hosp:1", "Whit Go1:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Whit Go1:1", "Gord Hosp:1" })) //
				.compileBothTripSort());
		map2.put(312l, new RouteTripSpec(312l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Oshawa Ctr Terminal", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "Coch Taun:1", "Ande Craw2:1", "Oc   Elmg3:1" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "Oc   Elmg3:1", "Dund Bowm:1", "Coch Taun:1" })) //
				.compileBothTripSort());
		map2.put(318l, new RouteTripSpec(318l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Taunton", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Shrs") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Whit Go1:1", "Gard Mead1:1", "Gard Taun3:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Gard Taun3:1", "Ross Coun2:1", "Jeff Dund2:1", "Whit Go1:1" })) //
				.compileBothTripSort());
		map2.put(414l, new RouteTripSpec(414l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Nonquon", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Oshawa Ctr Terminal") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "Dean Norm2:1", "Oc   Elmg3:1", "Nonq Mary:1", "Ormond D:1" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "Ormond D:1", "Hill Adel:1", "Oc   Elmg3:1", "Dean Norm1:1" })) //
				.compileBothTripSort());
		map2.put(501l, new RouteTripSpec(501l, //
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
						"Chur Temp1:1", //
								"Simp King:1", "Simp King:1_merged_922245", //
								"Mear Conc1:1", //
								"Long Libe:1", //
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"Long Libe:1", "Chur Temp1:1" //
						})) //
				.compileBothTripSort());
		map2.put(506l, new RouteTripSpec(506l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Bowmanville", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Orono") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { //
						"Main Wint:1", "Nort Grad:1", "Lake Wate:1", "Bowm Prin:1" //
						})) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { //
						"Bowm Prin:1", "Libe Vict:1", "Lake Wate:1", "Pete Taun:1", "Main Mill:1", "Main Wint:1" //
						})) //
				.compileBothTripSort());
		map2.put(601l, new RouteTripSpec(601l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Beaverton", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Uxbridge") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Welw Toro:1", // WELWOOD EASTBOUND @ 6 WELWOOD
								"1st Broc1", // !=
								"Broc Main2:1", // ==
								"Main Dall1:1", // !=
								"RR1 Rave1:1", // REGIONAL RD. 1 @ RAVENSHOE n ns
								"RR23 Sara:1", // !=
								"9Mil Lake:1" // 9 MILE @ LAKEVIEW MANNOR n ns
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"9Mil Lake:1", // 9 MILE @ LAKEVIEW MANNOR n ns
								"Main Mill1:1", // !=
								"RR23 Sara1:1", // !=
								"RR1 Rave2:1", // REGIONAL RD. 1 @ RAVENSHOE s fs
								"Firs Broc:1", // !=
								"Broc Main2:1", // ==
								"Toro Albe:1", // != TORONTO NORTHBOUND @ ALBERT
								"Toro Broc2:1", // != TORONTO SOUTHBOUND @ BROCK ST
								"Welw Toro:1" // WELWOOD EASTBOUND @ 6 WELWOOD
						})) //
				.compileBothTripSort());
		map2.put(701l, new RouteTripSpec(701l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "East Uxbridge Loop", //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, "West Uxbridge Loop") //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "Welw Toro:1", //
								"Toro Mill2:1", // ==
								"Reac East:1", // !=
								"Broc Main2:1", // !=
								"Toro Broc2:1", // ==
								"Welw Toro:1" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "Welw Toro:1", //
								"Toro Mill2:1", // ==
								"Toro Popl:1", // !=
								"Toro Broc1:1", // !=
								"Toro Broc2:1", // ==
								"Welw Toro:1" })) //
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
		map2.put(910l, new RouteTripSpec(910l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Durham College UOIT", //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, "Whitby Sta") //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { //
						"Whit Go1:1", "Oc   Elmg3:1", "Uoit Simc1:1" //
						})) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { //
						"Uoit Simc1:1", "Thor Ross2:1", "Oc   Elmg3:1", "Whit Go1:1" //
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
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
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
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()));
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
		if (mTrip.getRouteId() == 101l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Montgomery Pk Rd", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 103l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Rouge Hl Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 112l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("Pickering Sta", mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("William Jackson Dr", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 181l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Pickering West", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 223l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("Pickering Beach", mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Pickering Pkwy Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 291l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Pickering Town Ctr", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 292l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("Pickering Town Ctr", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 310l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("Durham College UOIT", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 401l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("Durham College UOIT", mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Lakeview Pk & Ritson", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 403l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("Oshawa Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 405l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("Harmony Terminal", mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Oshawa Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 407l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Colonel Sam Dr", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 412l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("Oshawa Ctr Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 414l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Oshawa", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 501l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Aspen Spgs Loop", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 502l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Liberty Loop", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 506l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("Orono", mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Bowmanville", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 910l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Whitby Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 915l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("Durham College UOIT", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 916l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Harmony Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 922l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("Harmony Terminal", mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Whitby Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 950l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("Uxbridge", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 103l + RID_ENDS_WITH_B) { // 103B
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("Pickering Sta", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 402l + RID_ENDS_WITH_B) { // 402B
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("Oshawa Ctr Terminal", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 110l + RID_ENDS_WITH_SH) { // 110SH
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString("Pickering West", mTrip.getHeadsignId());
				return true;
			}
		}
		System.out.printf("\nUnexpected trips to merge: %s & %s!\n", mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	private static final Pattern TO = Pattern.compile("((^|\\W){1}(to)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final Pattern VIA = Pattern.compile("((^|\\W){1}(via)(\\W|$){1})", Pattern.CASE_INSENSITIVE);

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
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
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
		gStopName = gStopName.toLowerCase(Locale.ENGLISH);
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
		if (gStop.getStopId().equalsIgnoreCase("Stro Tree1:1")) {
			return 1000001546;
		} else if (gStop.getStopId().equalsIgnoreCase("King Live3:1")) {
			return 1000001609;
		} else if (gStop.getStopId().equalsIgnoreCase("Came Ann1:1")) {
			return 1000002533;
		} else if (gStop.getStopId().equalsIgnoreCase("Audl Horn:1")) {
			return 1000000001;
		} else if (gStop.getStopId().equalsIgnoreCase("OldK Eliz:1")) {
			return 1000000002;
		} else if (gStop.getStopId().equalsIgnoreCase("Stev Crei1:1")) {
			return 1000000003;
		} else if (gStop.getStopId().equalsIgnoreCase("Harm Capr:1")) {
			return 1000000004;
		} else if (gStop.getStopId().equalsIgnoreCase("Park Buen:1")) {
			return 1000000005;
		} else if (gStop.getStopId().equalsIgnoreCase("Main Duch:1")) {
			return 1000000006;
		} else if (gStop.getStopId().equalsIgnoreCase("Main Wint:1")) {
			return 1000000007;
		} else if (gStop.getStopId().equalsIgnoreCase("Adel Came:1")) {
			return 1000000008;
		} else if (gStop.getStopId().equalsIgnoreCase("Broc Quak:1")) {
			return 1000000009;
		} else if (gStop.getStopId().equalsIgnoreCase("Main Dall:1")) {
			return 1000000010;
		} else if (gStop.getStopId().equalsIgnoreCase("Audl Horn1:1")) {
			return 1000000011;
		} else if (gStop.getStopId().equalsIgnoreCase("Wils Shak1:1")) {
			return 1000000012;
		} else if (gStop.getStopId().equalsIgnoreCase("Wils Shak:1")) {
			return 1000000013;
		} else if (gStop.getStopId().equalsIgnoreCase("Base RR5711")) {
			return 1000000014;
		}
		System.out.printf("\nUnexptected stop ID for %s!\n", gStop);
		System.exit(-1);
		return -1;
	}
}
