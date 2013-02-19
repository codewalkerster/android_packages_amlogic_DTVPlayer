package com.amlogic.DTVPlayer;


public class DbTransponder{

	private int scan_id;
	private int ts_no;
	private int id;
	private int sat_id;
	private int frequency;
	private int symbol_rate;
	private int polarization;
	private int fec_inner;
	private int db_id;

	private boolean SelectedFlag=false;

	public void setDbId(int db_id){
		this.db_id = db_id;
	}

	public int getDbId(){
		return this.db_id;
	}
	
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
	
     public int getTsNo() {
        return ts_no;
    }

    public void setTsNo(int ts_no) {
        this.ts_no = ts_no;
    }		

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

   public int getSatId() {
        return sat_id;
    }

    public void setSatId(int id) {
        this.sat_id = id;
    }

      public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }	

	  public int getSymbol() {
        return symbol_rate;
    }

    public void setSymbol(int Symbol) {
        this.symbol_rate = Symbol;
    }	

	  public int getPolarization() {
        return polarization;
    }

    public void setPolarization(int polarization) {
        this.polarization = polarization;
    }	

    	  public int getFecInner() {
        return fec_inner;
    }

    public void setFecInner(int fec_inner) {
        this.fec_inner = fec_inner;
    }		 

}

