package net.cbaines.suma;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "directions")
public class Direction {

    public final static String ROUTE_ID_FIELD_NAME = "route_id";

    @DatabaseField(generatedId = true)
    int id;

    @DatabaseField(canBeNull = false)
    String direction;

    @DatabaseField(foreign = true, columnName = ROUTE_ID_FIELD_NAME)
    BusRoute route;

    Direction() {
    }

    Direction(String dir, BusRoute route) {
	this.direction = dir;
	this.route = route;
    }

    public String toString() {
	return direction;
    }
}
