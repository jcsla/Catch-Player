package com.ffmpegtest;

import android.graphics.Bitmap;

public class PPLData {
	private String store_link = "";
	private Bitmap product_image;
	private int price = 0;
	private int product_code = 0;
	private String drama_code = "";
	private String brand_name = "";
	public String getStore_link() {
		return store_link;
	}
	public void setStore_link(String store_link) {
		this.store_link = store_link;
	}
	public Bitmap getProduct_image() {
		return product_image;
	}
	public void setProduct_image(Bitmap product_image) {
		this.product_image = product_image;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public int getProduct_code() {
		return product_code;
	}
	public void setProduct_code(int product_code) {
		this.product_code = product_code;
	}
	public String getDrama_code() {
		return drama_code;
	}
	public void setDrama_code(String drama_code) {
		this.drama_code = drama_code;
	}
	public String getBrand_name() {
		return brand_name;
	}
	public void setBrand_name(String brand_name) {
		this.brand_name = brand_name;
	}
	public String getProduct_name() {
		return product_name;
	}
	public void setProduct_name(String product_name) {
		this.product_name = product_name;
	}
	private String product_name = "";
}
