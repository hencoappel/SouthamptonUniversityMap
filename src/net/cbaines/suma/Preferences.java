package net.cbaines.suma;

public interface Preferences {
    static final String GPS_ENABLED = "GPSEnabled";
    static final boolean GPS_ENABLED_BY_DEFAULT = true;
    static final String UNI_LINK_BUS_TIMES = "uniLinkLiveBusTimesEnabled";
    static final boolean UNI_LINK_BUS_TIMES_ENABLED_BY_DEFAULT = true;
    static final String NON_UNI_LINK_BUS_TIMES = "nonUniLinkLiveBusTimesEnabled";
    static final boolean NON_UNI_LINK_BUS_TIMES_ENABLED_BY_DEFAULT = false;
    static final String UNI_LINK_BUS_STOPS = "uniLinkBusStop";
    static final boolean UNI_LINK_BUS_STOPS_ENABLED_BY_DEFAULT = true;
    static final String NON_UNI_LINK_BUS_STOPS = "nonUniLinkBusStop";
    static final boolean NON_UNI_LINK_BUS_STOPS_ENABLED_BY_DEFAULT = false;
}
