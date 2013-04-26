package beetle.generate.genbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import beetle.generate.conf.Configuration;
import beetle.generate.db.DBOperate;
import beetle.generate.file.FileOperate;
import beetle.generate.util.Common;

public class GenVO {
	private StringBuffer sb;
	private String packageName;
	private String className;
	private HashMap attributes;

	public GenVO(String packageName, String className, HashMap attributes) {
		this.packageName = packageName;
		this.className = className;
		this.attributes = attributes;
		this.sb = new StringBuffer();
		genClass();
	}

	public void genClassHeader() { // �����ͷ����
		sb.append("package ").append(packageName).append(".valueobject")
				.append(";\n");
		sb.append("import java.sql.Date;\n");
		sb.append("import java.sql.Timestamp;\n");
		sb.append("import java.math.BigDecimal;\n");
		sb.append("\n\n");
	}

	public void genAttributes() { // ��������Բ���

		String attributeName = null;
		String attributeType = null;
		// Type type = Type.getInstance();
		Iterator it = attributes.keySet().iterator();
		sb.append("\n");
		sb.append("    private static final long serialVersionUID = 1L;\n");
		while (it.hasNext()) {
			Object obj = it.next();
			if (!(obj.toString().equals("tabName"))
					&& !(obj.toString().equals("primaryKey"))) {
				attributeName = obj.toString().toLowerCase();
				attributeType = attributes.get(obj).toString();
				sb.append("    private ").append(attributeType).append(" ")
						.append(attributeName).append(";");
				sb.append("\n");

			}

		}

	}

	public void genConstructor() { // ����๹�캯��
		sb.append("\n");
		sb.append("    public ").append(Common.genTableClassName(className))
				.append("(){");
		sb.append("\n");
		sb.append("    }");
		sb.append("\n");
	}

	public void genGetters() { // �����getter����

		String attributeName = null;
		String attributeType = null;
		// Type type = Type.getInstance();
		Iterator it = attributes.keySet().iterator();
		sb.append("\n");
		while (it.hasNext()) {
			Object obj = it.next();
			if (!(obj.toString().equals("tabName"))
					&& !(obj.toString().equals("primaryKey"))) {
				attributeName = obj.toString();
				attributeType = attributes.get(obj).toString();
				sb.append("\n");
				sb.append("    public ").append(attributeType).append(" get")
						.append(Common.fisrtCharToUpCase3(attributeName))
						.append("(){");
				sb.append("\n");
				sb.append("      return this.")
						.append(attributeName.toLowerCase()).append(";");
				sb.append("\n");
				sb.append("    }");
			}
		}

	}

	public void genSetters() { // �����setter����

		String attributeName = null;
		String attributeType = null;
		// Type type = Type.getInstance();
		Iterator it = attributes.keySet().iterator();
		sb.append("\n");
		while (it.hasNext()) {
			Object obj = it.next();
			if (!(obj.toString().equals("tabName"))
					&& !(obj.toString().equals("primaryKey"))) {
				attributeName = obj.toString();
				attributeType = attributes.get(obj).toString();

				sb.append("\n");
				sb.append("    public void set")
						.append(Common.fisrtCharToUpCase3(attributeName))
						.append("(").append(attributeType).append(" ")
						.append(attributeName.toLowerCase()).append("){");
				sb.append("\n");
				sb.append("      this.").append(attributeName.toLowerCase())
						.append(" = ").append(attributeName.toLowerCase())
						.append(";");
				sb.append("\n");
				sb.append("    }");
			}
		}

	}

	public void genClassBody() { // ��������岿��
		sb.append("public class ").append(Common.genTableClassName(className))
				.append(" implements java.io.Serializable{");
		sb.append("\n");
		genAttributes();
		genConstructor();
		genGetters();
		genSetters();
		sb.append("\n");
		sb.append("}");
	}

	public void genClass() { // �������������
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
		String dir = cfg.getValue("java.outPath") + "valueobject\\";
		FileOperate f = new FileOperate();
		DBOperate dbOperate = new DBOperate();
		GenVOBase genVOBase = new GenVOBase();
		f.createFile(genVOBase.getSb(), dir, "VOBase.java");
		ArrayList arrayList = dbOperate.getAllTbFields();
		for (int i = 0; i < arrayList.size(); i++) {
			System.out.println(i);
			HashMap tbFields = (HashMap) arrayList.get(i);
			tabName = tbFields.get("tabName").toString();
			GenVO genVO = new GenVO(packageName, tabName, tbFields);

			fileName = Common.fisrtCharToUpCase(tabName) + ".java";
			f.createFile(genVO.getSb(), dir, fileName);
			System.out.println(genVO.getSb());
		}

	}

}
