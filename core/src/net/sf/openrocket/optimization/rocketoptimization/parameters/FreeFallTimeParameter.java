package net.sf.openrocket.optimization.rocketoptimization.parameters;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.unit.UnitGroup;

public class FreeFallTimeParameter extends SimulationBasedParameter {
	
	private final double g = -9.81;
	private final double tolerence = 0.05;
	
	private final double highVal, lowVal;
	
	public FreeFallTimeParameter() {
		highVal = g * (1 - tolerence);
		lowVal = g * (1 + tolerence);
	}
	
	@Override
	public String getName() {
		return "Free Fall Time";
	}
	
	@Override
	public UnitGroup getUnitGroup() {
		return UnitGroup.UNITS_FLIGHT_TIME;
	}
	
	@Override
	protected double getResultValue(FlightData simulatedData) {
		List<Double> accels = simulatedData.getBranch(0).get(FlightDataType.TYPE_ACCELERATION_Z);
		List<Double> times = simulatedData.getBranch(0).get(FlightDataType.TYPE_TIME);
		return getResultValue(times, accels);
	}
	
	private double getResultValue(List<Double> times, List<Double> accels) {
		ArrayList<Double> durations = new ArrayList<Double>();
		double entrance = -1;
		
		for (int i = 0; i < accels.size(); i++) {
			double accel = accels.get(i);
			if (accel >= lowVal && accel <= highVal) {
				if (entrance == -1) {
					entrance = times.get(i);
				}
			} else if (entrance != -1) {
				durations.add(times.get(i) - entrance);
				entrance = -1;
			}
		}
		
		double maxDuration = 0;
		for (Double dur : durations) {
			if (dur > maxDuration) {
				maxDuration = dur;
			}
		}
		
		return maxDuration;
	}
	
}
