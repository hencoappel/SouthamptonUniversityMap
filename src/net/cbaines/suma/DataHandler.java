/*
 * Southampton University Map App
 * Copyright (C) 2011  Christopher Baines
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.cbaines.suma;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.overlay.PathOverlay;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class DataHandler extends DefaultHandler {

    // this holds the data
    private PathOverlay _data;

    private int colour;
    private ResourceProxy resProxy;

    public DataHandler(int colour, ResourceProxy resProxy) {
	this.colour = colour;
	this.resProxy = resProxy;
    }

    /**
     * Returns the data object
     * 
     * @return
     */
    public PathOverlay getData() {
	return _data;
    }

    /**
     * This gets called when the xml document is first opened
     * 
     * @throws SAXException
     */
    @Override
    public void startDocument() throws SAXException {
	_data = new PathOverlay(colour, resProxy);
    }

    /**
     * Called when it's finished handling the document
     * 
     * @throws SAXException
     */
    @Override
    public void endDocument() throws SAXException {

    }

    /**
     * This gets called at the start of an element. Here we're also setting the booleans to true if it's at that specific tag. (so we know where we are)
     * 
     * @param namespaceURI
     * @param localName
     * @param qName
     * @param atts
     * @throws SAXException
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
	if (localName.equals("trkpt")) {
	    // Log.v("DataHandler", "Adding point to route overlay " + atts.getValue("lat") + " " + atts.getValue("lon"));
	    _data.addPoint(Util.csLatLongToGeoPoint(atts.getValue("lat"), atts.getValue("lon")));
	}
    }

    /**
     * Called at the end of the element. Setting the booleans to false, so we know that we've just left that tag.
     * 
     * @param namespaceURI
     * @param localName
     * @param qName
     * @throws SAXException
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

    }

    /**
     * Calling when we're within an element. Here we're checking to see if there is any content in the tags that we're interested in and populating it in the
     * Config object.
     * 
     * @param ch
     * @param start
     * @param length
     */
    @Override
    public void characters(char ch[], int start, int length) {

    }
}
