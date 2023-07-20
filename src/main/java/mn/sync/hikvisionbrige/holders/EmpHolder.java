package mn.sync.hikvisionbrige.holders;

import mn.sync.hikvisionbrige.models.Employee;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 18/07/2023 - 3:28 PM
 * @purpose
 * @definition
 */
public final class EmpHolder {

    private Employee employee;
    private final static EmpHolder INSTANCE = new EmpHolder();

    private EmpHolder() {}

    public static EmpHolder getInstance() {
        return INSTANCE;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Employee getEmployee() {
        return this.employee;
    }
}
