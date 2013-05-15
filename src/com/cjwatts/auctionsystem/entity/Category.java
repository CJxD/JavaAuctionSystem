package com.cjwatts.auctionsystem.entity;

public enum Category {
	ANTIQUES ("Antiques"),
	AUTOMOTIVE ("Automotive"),
	ART ("Art"),
	BABY ("Baby"),
	BOOKS ("Books & Magazines"),
	CLOTHING ("Clothing"),
	COLLECTABLES ("Collectables"),
	COMPUTING ("Computing"),
	CONSUMER_ELECTRONICS ("Consumer Electronics"),
	CRAFTS ("Crafts"),
	DOLLS ("Dolls"),
	HOME_AND_GARDEN ("Home & Garden"),
	JEWELLERY ("Jewellery & Watches"),
	LOCAL_SERVICES ("Local Services"),
	MISC ("Miscellaneous"),
	MUSIC ("Music & Intruments"),
	OFFICE ("Office"),
	PHOTOGRAPHY ("Photography"),
	POTTERY_AND_GLASS ("Pottery & Glass"),
	SPORTING_GOODS ("Sporting Goods"),
	STAMPS ("Stamps"),
	TICKETS ("Tickets & Travel"),
	TV_AND_VIDEO ("TV, Film & Video"),
	TOYS_AND_GAMES ("Toys & Games"),
	VIDEO_GAMES ("Video Games");
	
	private String friendlyName;
	
	private Category(String friendlyName) {
		this.friendlyName = friendlyName;
	}
	
	@Override
	public String toString() {
		return friendlyName;
	}
}
