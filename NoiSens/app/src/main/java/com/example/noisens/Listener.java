package com.example.noisens;

public interface Listener {
    public void onResultReceived(String strRM, String strRR);
    public void sendDP(float dP);
    public void sendDD(float dD);
    public void sendQosP(double qp);
    public void sendQosD(double qd);
    public void sendQosN(double qn);

}
