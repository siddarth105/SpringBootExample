package com.sboot.mserv.casestudy.callcenter.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sboot.mserv.casestudy.callcenter.model.associate.Associate;
import com.sboot.mserv.casestudy.callcenter.model.associate.AssociateFactory;
import com.sboot.mserv.casestudy.callcenter.model.associate.ManagerDto;
import com.sboot.mserv.casestudy.callcenter.model.call.CallDetails;
import com.sboot.mserv.casestudy.callcenter.model.call.CallReport;
import com.sboot.mserv.casestudy.callcenter.model.call.PerformanceDto;
import com.sboot.mserv.casestudy.callcenter.util.CallCenterConstants;

@Service
public class CallDetailServiceImpl implements CallDetailService{
	
	private final Log log = LogFactory.getLog(getClass());
	
	@Value("${assoc.je.sla}")
	private int assocJeSLA;
	
	@Value("${assoc.se.sla}")
	private int assocSeSLA;
	
	@Value("${assoc.mgr.sla}")
	private int mgrSLA;
	
	Associate asc = null;
	int reslv, escl, id, timeTaken;
	int totalReslvCalls, totalEsclCalls, totalTimeTaken, totalCalls, numOfCalls;

	@Override
	public CallReport getReport(CallDetails callDetails) {
		this.log.info("CallDetailServiceImpl :: getReport");
		totalCalls = getNumberOfCall(callDetails.getJeLst()) + getNumberOfCall(callDetails.getSeLst()) + callDetails.getMgrLst().size();
		int callThreshold = totalCalls / (callDetails.getJeLst().size() + callDetails.getSeLst().size());
		this.log.info("totalCalls :: " + totalCalls);
		this.log.info("callThreshold :: " + callThreshold);
		List<Associate> juniorExecutiveList = populateAssLst(callDetails.getJeLst(), CallCenterConstants.JE, assocJeSLA, callThreshold);
		List<Associate> seniorExecutiveList = populateAssLst(callDetails.getSeLst(), CallCenterConstants.SE, assocSeSLA, callThreshold);
		ManagerDto mgrDto = populateMgrDto(callDetails.getMgrLst());
	
		CallReport callReport = generateReport(juniorExecutiveList, seniorExecutiveList, mgrDto);
		return callReport;
	}
	
	private int getNumberOfCall (List<List<Integer>> assocLst) {
		numOfCalls = 0;
		assocLst.forEach(lst -> {
			numOfCalls = lst.size() + numOfCalls;
			});
		
		return numOfCalls;
	}
	
	
	private List<Associate> populateAssLst(List<List<Integer>> ascLst, String associateType, int assocSla, int callThreshold) {
		
		List<Associate> empLst = new ArrayList<Associate>();
		id = 0;
		ascLst.forEach(jeLst -> {
			reslv = 0; escl = 0; timeTaken = 0;
			asc = AssociateFactory.getAssociate(associateType, callThreshold);
			asc.setCallsAttended(jeLst.size());
			jeLst.forEach(time -> {
				if (time <= assocSla) {
					reslv++;
					totalReslvCalls++;
				} else {
					escl++;
					totalEsclCalls++;
				}
				timeTaken += time; 
				totalTimeTaken += time;
			});
			asc.setEscCalls(escl);
			asc.setRslvCalls(reslv);
			asc.setId(id++);
			asc.setTimeTaken(timeTaken);
			empLst.add(asc);
		});
		return empLst;
	}
	
	private ManagerDto populateMgrDto(List<Integer> mgrLst) {
		
		reslv = 0; escl = 0; timeTaken = 0; id = 0;
		ManagerDto managerDto = new ManagerDto();
		mgrLst.forEach(time -> {
			if (time <= mgrSLA) {
				reslv++;
				totalReslvCalls++;
			} else {
				escl++;
				totalEsclCalls++;
			}
			timeTaken += time; 
			totalTimeTaken += time;
		});
		managerDto.setUnRslvCalls(escl);;
		managerDto.setRslvCalls(reslv);
		managerDto.setId(id++);
		managerDto.setCallsAttended(mgrLst.size());
		managerDto.setTimeTaken(timeTaken);
		return managerDto;
	}
	
	private CallReport generateReport(List<Associate> juniorExecutiveList, List<Associate> seniorExecutiveList, ManagerDto mgrDto) {
		
		this.log.info("CallDetailServiceImpl :: generateReport");
		
		PerformanceDto performanceDto = new PerformanceDto();
		performanceDto.setJuniorExecutiveList(juniorExecutiveList);
		performanceDto.setSeniorExecutiveList(seniorExecutiveList);
		performanceDto.setManagerList(mgrDto);
		
		CallReport callReport = new CallReport();
		callReport.setPerformance(performanceDto);
		callReport.setNumberOfCalls(totalCalls);
		callReport.setResolvedCalls(totalReslvCalls);
		callReport.setUnresolvedCalls(totalEsclCalls);
		callReport.setTotalTimeTaken(totalTimeTaken);
		return callReport;
		
	}

}
