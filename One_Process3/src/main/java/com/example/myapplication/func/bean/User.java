package com.example.myapplication.func.bean;

/**
 * Created by Long on 2016/7/30.
 */
public class User {
	private static User mUser;

	private int id;
	private String name;
	private String tel;
	private String face;
	private String sex;

	private boolean isLogin = false;

	public static User getUser(){
		if(mUser == null){
			mUser = new User();
		}
		return mUser;
	}

	public User(){}
	public User(int id, String name, String tel, String face, String sex){
		setId(id);
		setName(name);
		setTel(tel);
		setFace(face);
		setSex(sex);
		mUser = this;
		mUser.isLogin = true;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getFace() {
		return face;
	}

	public void setFace(String face) {
		this.face = face;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public boolean isLogin() {
		return isLogin;
	}


	public void logout(){
		setName("");
		setFace("");
		setSex("");
		setTel("");
		isLogin = false;
	}

}
