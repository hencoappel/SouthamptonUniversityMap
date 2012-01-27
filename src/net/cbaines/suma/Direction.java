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

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((direction == null) ? 0 : direction.hashCode());
	result = prime * result + ((route == null) ? 0 : route.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Direction other = (Direction) obj;
	if (direction == null) {
	    if (other.direction != null)
		return false;
	} else if (!direction.equals(other.direction))
	    return false;
	if (route == null) {
	    if (other.route != null)
		return false;
	} else if (!route.equals(other.route))
	    return false;
	return true;
    }
}
