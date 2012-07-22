package beetle.generate.genbean;

import java.util.ArrayList;
import java.util.HashMap;

import beetle.generate.conf.Configuration;
import beetle.generate.db.DBOperate;
import beetle.generate.file.FileOperate;
import beetle.generate.util.Common;

public class GenDao {
	private StringBuffer sb;
	private String className;
	private String packageName;
	private String exception;
	private HashMap attributes;

	public GenDao(String packageName, String className, HashMap attributes) {
		this.packageName = packageName;
		this.className = className;
		this.attributes = attributes;
		this.sb = new StringBuffer();
		Configuration cfg = Configuration.getInstance();
		this.exception = cfg.getValue("java.exceptionName");
		genClass();
	}

	public String getPK() {
		return attributes.get("primaryKey").toString();
	}

	public String getPKType() {
		return (String) attributes.get(getPK());
	}

	public void genClassHeader() { // �����ͷ����
		sb.append("package ").append(packageName).append(".persistence.dao")
				.append(";");
		sb.append("\n\n");
		sb.append("import java.util.*;");
		sb.append("\n\n");
		sb.append("import com.beetle.framework.persistence.access.operator.DBOperatorException;");
		sb.append("\n\n");
		sb.append("import ").append(packageName).append(".valueobject.*;");
		sb.append("\n");
		sb.append("import ").append(packageName).append(".persistence.*;");
		sb.append("\n\n");
	}

	public void genMethods() { // ����෽������
		sb.append("\n");
		sb.append("    ").append(Common.genTableClassName(className))
				.append(" get").append("(").append(getPKType()).append(" ")
				.append(Common.fisrtCharToLowerCase(getPK()))
				.append(")throws DBOperatorException;"); // ��ѯ��������
		sb.append("\n");

		sb.append(
				"    List<" + Common.genTableClassName(className) + "> getAll")
				.append("()throws DBOperatorException;"); // ��ѯ�б?��
		sb.append("\n");

		sb.append("    int insert")
				.append("(")
				.append(Common.genTableClassName(className))
				.append(" ")
				.append(Common.fisrtCharToLowerCase2(Common
						.fisrtCharToUpCase(className)))
				.append(")throws DBOperatorException;"); // ���뷽��
		sb.append("\n");

		sb.append("    int update")
				.append("(")
				.append(Common.genTableClassName(className))
				.append(" ")
				.append(Common.fisrtCharToLowerCase2(Common
						.fisrtCharToUpCase(className)))
				.append(")throws DBOperatorException;"); // �޸ķ���
		sb.append("\n");

		sb.append("    int delete").append("(").append(getPKType()).append(" ")
				.append(Common.fisrtCharToLowerCase(getPK()))
				.append(")throws DBOperatorException;"); // ɾ��
		sb.append("\n");

	}

	public void genMethods_bak2() { // ����෽������
		sb.append("\n");
		sb.append("    ").append(Common.genTableClassName(className))
				.append(" get").append(Common.fisrtCharToUpCase(className))
				.append("(").append(getPKType()).append(" ")
				.append(Common.fisrtCharToLowerCase(getPK())).append(");"); // ��ѯ��������
		sb.append("\n");

		sb.append("    List get").append(Common.fisrtCharToUpCase(className))
				.append("List").append("();"); // ��ѯ�б?��
		sb.append("\n");

		sb.append("    int insert")
				.append(Common.fisrtCharToUpCase(className))
				.append("(")
				.append(Common.genTableClassName(className))
				.append(" ")
				.append(Common.fisrtCharToLowerCase2(Common
						.fisrtCharToUpCase(className))).append(");"); // ���뷽��
		sb.append("\n");

		sb.append("    int update")
				.append(Common.fisrtCharToUpCase(className))
				.append("(")
				.append(Common.genTableClassName(className))
				.append(" ")
				.append(Common.fisrtCharToLowerCase2(Common
						.fisrtCharToUpCase(className))).append(");"); // �޸ķ���
		sb.append("\n");

		sb.append("    int delete").append(Common.fisrtCharToUpCase(className))
				.append("(").append(getPKType()).append(" ")
				.append(Common.fisrtCharToLowerCase(getPK())).append(");"); // ɾ��
		sb.append("\n");

	}

	public void genMethods_bak() { // ����෽������
		sb.append("\n");
		sb.append("    ").append(Common.fisrtCharToUpCase(className))
				.append(" get").append(Common.fisrtCharToUpCase(className))
				.append("(").append(getPKType()).append(" ")
				.append(Common.fisrtCharToLowerCase(getPK()))
				.append(") throws ").append(exception).append(";"); // ��ѯ��������
		sb.append("\n");

		sb.append("    List get").append(Common.fisrtCharToUpCase(className))
				.append("List").append("() throws ").append(exception)
				.append(";"); // ��ѯ�б?��
		sb.append("\n");

		sb.append("    void insert")
				.append(Common.fisrtCharToUpCase(className))
				.append("(")
				.append(Common.fisrtCharToUpCase(className))
				.append(" ")
				.append(Common.fisrtCharToLowerCase2(Common
						.fisrtCharToUpCase(className))).append(") throws ")
				.append(exception).append(";"); // ���뷽��
		sb.append("\n");

		sb.append("    void update")
				.append(Common.fisrtCharToUpCase(className))
				.append("(")
				.append(Common.fisrtCharToUpCase(className))
				.append(" ")
				.append(Common.fisrtCharToLowerCase2(Common
						.fisrtCharToUpCase(className))).append(") throws ")
				.append(exception).append(";"); // �޸ķ���
		sb.append("\n");

		sb.append("    void delete")
				.append(Common.fisrtCharToUpCase(className)).append("(")
				.append(getPKType()).append(" ")
				.append(Common.fisrtCharToLowerCase(getPK()))
				.append(") throws ").append(exception).append(";"); // ɾ��
		sb.append("\n");

	}

	public void genClassBody() { // ��������岿��
		sb.append("public interface I")
				.append(Common.fisrtCharToUpCase(className)).append("Dao{");
		this.genMethods();
		sb.append("\n");
		sb.append("}");
		sb.append("\n");
	}

	public void genClass() {
		genClassHeader();
		genClassBody();
	}

	public String getSb() {
		return sb.toString();
	}

	public static void main(String[] args) {
		Configuration cfg = Configuration.getInstance();
		String packageName = cfg.getValue("java.package");
		String tabName;
		String fileName;
		String dir = cfg.getValue("java.outPath") + "persistance\\dao\\";
		FileOperate f = new FileOperate();
		DBOperate dbOperate = new DBOperate();

		ArrayList arrayList = dbOperate.getAllTbFields();
		for (int i = 0; i < arrayList.size(); i++) {
			System.out.println(i);
			HashMap tbFields = (HashMap) arrayList.get(i);
			tabName = tbFields.get("tabName").toString();
			GenDao genDao = new GenDao(packageName, tabName, tbFields);

			fileName = "I" + Common.fisrtCharToUpCase(tabName) + "Dao.java";
			f.createFile(genDao.getSb(), dir, fileName);
			System.out.println(genDao.getSb());
		}
	}

}
