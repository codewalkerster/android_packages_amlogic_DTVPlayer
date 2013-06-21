package com.amlogic.DTVPlayer;

import java.util.List;
import java.util.ArrayList;

public class DbSat{

	private int scan_id;
	private int sat_no;
	private int sat_id;
	private String name;
	private int flags;

	private int angle_value;
	private int position;
	private int position_number;
	
	List<DbTransponder> transponder=null;

	private int LnbNo;	
	private int  LoLOF; // low LOF value,unit MHz
	private int HiLOF; // high LOF value,unit MHz
	private int LOFThreshold;
	private int LNBType;// Single LOF or Double LOF
	private int DiseqcLevel;
	private int ToneburstType;//
	private int Swt10Or11Port; //
	private int Swt22KOnOff;
	private int LNBPwrOnOff;
	private int MotoNo;
	private int MotoMode; // Motor position  bit8 1:USALS 0:DISEQC1.2
	private int SatLnbConfig10;
	private int SatLnbConfig11;

	private int FastDiseqc = 0;
	private int DiseqcRepeat = 0;
	private int DiseqcSequence = 0;

	private boolean SelectedFlag=false;

	private int lo_direction=0;
	private int la_direction=0;
	private double  longitude=0;
	private double latitude=0;
	private double sat_longitude=0;
	
	public final  static int LNB_POWER_13V=0;
	public final static int LNB_POWER_18V=1;
	public final static int LNB_POWER_OFF=2;
	public final static int LNB_POWER_13V18V=3;

	
	public final  static int LNB_22K_ON=0;
	public final static int  LNB_22K_OFF=1;
	public final static int LNB_22K_LOHI=2;

	public final static int LNB_TONEBURST_NONE=0;
	public final  static int LNB_TONEBURST_A=1;
	public final static int LNB_TONEBURST_B=2;

	public final static int LNB_DISEQC_NONE=0;
	public final static int LNB_DISEQC_10=1;
	public final static int LNB_DISEQC_11=2;
	public final static int LNB_DISEQC_12=3;
	public final static int LNB_DISEQC_13=4;

	public void setScanId(int scan_id){
		this.scan_id = scan_id;
	}

	public int getScanId(){
		return this.scan_id;
	}
	
	public void setSelectedFlag(boolean flag){
		this.SelectedFlag = flag;
	}
		
	public boolean getSelectedFlag(){
		return SelectedFlag;
	}
		
	public int getSatNo() {
	    return sat_no;
	}

	public void setSatNo(int sat_no) {
	    this.sat_no = sat_no;
	}
		
	public int getSatId() {
	    return sat_id;
	}

	public void setSatId(int id) {
	    this.sat_id = id;
	}

	public String getName() {
	    return name;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public int getFlags() {
	    return flags;
	}

	public void setFlags(int flags) {
	    this.flags = flags;
	}


	 public int getPosition() {
	    return position;
	}

	public void setPosition(int pos) {
	    this.position = pos;
	}	

	 public int getPositionNumber() {
	    return position_number;
	}

	public void setPositionNumber(int pos_no) {
	    this.position_number = pos_no;
	}	


	 public int getAngle() {
	    return angle_value;
	}

	public void setAngle(int value) {
	    this.angle_value= value;
	}	



	public void setLnbNo(int no){
		this.LnbNo=no;		
	}

	public int getLnbNo(){
		return this.LnbNo;
	}
	public void setLoLOF(int fre){
		this.LoLOF = fre;
	}

	public int getLoLOF(){
		return this.LoLOF;
	}

	public void setHiLOF(int fre){
		this.HiLOF = fre;
	}	

	public int getHiLOF(){
		return this.HiLOF;
	}	

	public void setLofThreshold(int fre){
		this.LOFThreshold = fre;
	}	

	public int getLofThreshold(){
		return this.LOFThreshold;
	}	

	public void setLNBType(int type){
		this.LNBType = type;
	}

	public int getLNBType(){
		return this.LNBType;
	}
	
	public void setToneburstType(int type){
		this.ToneburstType = type;
	}

	public int getToneburstType(){
		return this.ToneburstType;
	}

	public void setSwtPort(int type){
		this.Swt10Or11Port = type;
	}

	public int getSwtPort(){
		return this.Swt10Or11Port;
	}

	public void set22KOnOff(int mode){
		this.Swt22KOnOff = mode;
	}

	public int get22KOnOff(){
		return this.Swt22KOnOff;
	}

	public void setLNBPwrOnOff(int mode){
		this.LNBPwrOnOff = mode;
	}

	public int getLNBPwrOnOff(){
		return this.LNBPwrOnOff;
	}

	public void setLnbConfig10(int lnb){
		this.SatLnbConfig10 = lnb;	
	}

	public int getLnbConfig10(){
		return this.SatLnbConfig10;
	}

	public void setLnbConfig11(int lnb){
		this.SatLnbConfig11 = lnb;	
	}

	public int getLnbConfig11(){
		return this.SatLnbConfig11;
	}


	public void setFastDiseqc(int mode){
		this.FastDiseqc = mode;	
	}

	public int getFastDiseqc(){
		return this.FastDiseqc;
	}


	public void setDiseqcRepeat(int mode){
		this.DiseqcRepeat = mode;	
	}

	public int getDiseqcRepeat(){
		return this.DiseqcRepeat;
	}

	public void setDiseqcSequence(int mode){
		this.DiseqcSequence = mode;	
	}

	public int getDiseqcSequence(){
		return this.DiseqcSequence;
	}
	
	public void setMotMode(int mode){
		this.MotoMode = mode;	
	}

	public int getMotoMode(){
		return this.MotoMode; // Motor position  bit8 1:USALS 0:DISEQC1.2
	}


	public void setMotoNo(int mode){
		this.MotoNo = mode;	
	}

	public int getMotoNo(){
		return this.MotoNo; 
	}

	public void setLongitudeDirection(int value){
		this.lo_direction=value;
	}

	public int getLongitudeDirection(int value){
		return this.lo_direction;
	}

	public void setLatitudeDirection(int value){
		this.la_direction=value;
	}

	public int getLatitudeDirection(){
		return this.la_direction;
	}
	
	public void setLongitude(double value){
		this.longitude=value;
	}

	public double getLongitude(){
		return this.longitude;
	}
	
	public void setLatitude(double value){
		this.longitude=value;
	}

	public double getLatitude(){
		return this.longitude;
	}
	
	
	public void setSatLongitude(double value){
		this.sat_longitude=value;
	}

	public double getSatLongitude(){
		return this.sat_longitude;
	}
	
}

