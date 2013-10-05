package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import net.sf.openrocket.database.motor.ThrustCurveMotorSet;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.rocketcomponent.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.MotorMount;

////////  Row filters

/**
 * Abstract adapter class.
 */
class MotorRowFilter extends RowFilter<TableModel, Integer> {

	public enum DiameterFilterControl {
		ALL,
		EXACT,
		SMALLER
	};

	// configuration data used in the filter process
	private final ThrustCurveMotorDatabaseModel model;
	private Double diameter;
	private List<ThrustCurveMotor> usedMotors = new ArrayList<ThrustCurveMotor>();

	// things which can be changed to modify filter behavior

	private Double maximumLength;

	// Limit motors based on minimum diameter
	private Double minimumDiameter;

	// Collection of strings which match text in the motor
	private List<String> searchTerms = Collections.<String> emptyList();

	// Limit motors based on diameter of the motor mount
	private DiameterFilterControl diameterControl = DiameterFilterControl.ALL;

	// Boolean which hides motors in the usedMotors list
	private boolean hideUsedMotors = false;

	// List of manufacturers to exclude.
	private List<Manufacturer> excludedManufacturers = new ArrayList<Manufacturer>();

	// List of ImpulseClasses to exclude.
	private List<ImpulseClass> excludedImpulseClass = new ArrayList<ImpulseClass>();

	public MotorRowFilter(ThrustCurveMotorDatabaseModel model) {
		super();
		this.model = model;
	}

	public void setMotorMount( MotorMount mount ) {
		if (mount != null) {
			this.diameter = mount.getMotorMountDiameter();
			for (MotorConfiguration m : mount.getMotorConfiguration()) {
				this.usedMotors.add((ThrustCurveMotor) m.getMotor());
			}
		} else {
			this.diameter = null;
		}
	}

	public void setSearchTerms(final List<String> searchTerms) {
		this.searchTerms = new ArrayList<String>();
		for (String s : searchTerms) {
			s = s.trim().toLowerCase(Locale.getDefault());
			if (s.length() > 0) {
				this.searchTerms.add(s);
			}
		}
	}

	Double getMaximumLength() {
		return maximumLength;
	}

	void setMaximumLength(Double maximumLength) {
		this.maximumLength = maximumLength;
	}

	Double getMinimumDiameter() {
		return minimumDiameter;
	}

	void setMinimumDiameter(Double minimumDiameter) {
		this.minimumDiameter = minimumDiameter;
	}

	DiameterFilterControl getDiameterControl() {
		return diameterControl;
	}

	void setDiameterControl(DiameterFilterControl diameterControl) {
		this.diameterControl = diameterControl;
	}

	void setHideUsedMotors(boolean hideUsedMotors) {
		this.hideUsedMotors = hideUsedMotors;
	}

	List<Manufacturer> getExcludedManufacturers() {
		return excludedManufacturers;
	}

	void setExcludedManufacturers(Collection<Manufacturer> excludedManufacturers) {
		this.excludedManufacturers.clear();
		this.excludedManufacturers.addAll(excludedManufacturers);
	}

	void setExcludedImpulseClasses(Collection<ImpulseClass> excludedImpulseClasses ) {
		this.excludedImpulseClass.clear();
		this.excludedImpulseClass.addAll(excludedImpulseClasses);
	}

	@Override
	public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
		int index = entry.getIdentifier();
		ThrustCurveMotorSet m = model.getMotorSet(index);
		return filterManufacturers(m) && filterUsed(m) && filterBySize(m) && filterByString(m) && filterByImpulseClass(m);
	}

	private boolean filterManufacturers(ThrustCurveMotorSet m) {
		if (excludedManufacturers.contains(m.getManufacturer())) {
			return false;
		} else {
			return true;
		}
	}

	private boolean filterUsed(ThrustCurveMotorSet m) {
		if (!hideUsedMotors) {
			return true;
		}
		for (ThrustCurveMotor motor : usedMotors) {
			if (m.matches(motor)) {
				return false;
			}
		}
		return true;
	}

	private boolean filterBySize(ThrustCurveMotorSet m) {

		if ( minimumDiameter != null ) {
			if ( m.getDiameter() <= minimumDiameter - 0.0015 ) {
				return false;
			}
		}

		if (diameter != null) {
			switch (diameterControl) {
			default:
			case ALL:
				break;
			case EXACT:
				if ((m.getDiameter() <= diameter + 0.0004) && (m.getDiameter() >= diameter - 0.0015)) {
					break;
				}
				return false;
			case SMALLER:
				if (m.getDiameter() <= diameter + 0.0004) {
					break;
				}
				return false;
			}
		}
		
		if ( maximumLength != null ) {
			if ( m.getLength() > maximumLength ) {
				return false;
			}
		}
		
		return true;
	}


	private boolean filterByString(ThrustCurveMotorSet m) {
		main: for (String s : searchTerms) {
			for (ThrustCurveMotorColumns col : ThrustCurveMotorColumns.values()) {
				String str = col.getValue(m).toString().toLowerCase(Locale.getDefault());
				if (str.indexOf(s) >= 0)
					continue main;
			}
			return false;
		}
	return true;
	}

	private boolean filterByImpulseClass(ThrustCurveMotorSet m) {
		for( ImpulseClass c : excludedImpulseClass ) {
			if (c.isIn(m) ) {
				return false;
			}
		}
		return true;
	}

}
