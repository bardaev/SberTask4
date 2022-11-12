import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, TransformerException {
        InputStream in = Main.class.getClassLoader()
                .getResourceAsStream("file.xml");

        Bank bank = new Bank();

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (qName.equalsIgnoreCase("Bank")) {
                        bank.setWallet(BigDecimal.valueOf(Double.parseDouble(attributes.getValue(0))));
                    }

                    if (qName.equalsIgnoreCase("Person")) {
                        Person person = new Person(
                                attributes.getValue(0),
                                BigDecimal.valueOf(Double.parseDouble(attributes.getValue(1)))
                        );
                        bank.getPersonList().add(person);
                    }
                }
            };

            parser.parse(in, handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
        compute(bank);

        writeResult(bank);
    }

    /*
        Отсортируем массив и далее будем выравнивать счета, добавляя по еденице к счету, у которого самая меньшая сумма,
        и вычитая из счета банка.
    */
    public static void compute(Bank bank) {
        BigDecimal countPerson = BigDecimal.valueOf(bank.getPersonList().size());
        bank.getPersonList().sort(new PersonComparator());

        BigDecimal sumBank = bank.getWallet();

        while (isMoreZero(sumBank)) {

            if (!isListAlign(bank.getPersonList()) && isMoreZero(sumBank)) {
                sumBank = alignmentWallet(bank.getPersonList(), sumBank);
            } else {
                BigDecimal plusWallet = sumBank.divide(countPerson, RoundingMode.DOWN).setScale(0, RoundingMode.DOWN);
                if (plusWallet.compareTo(BigDecimal.ONE) > 0) {
                    sumBank = sumBank.subtract(plusWallet);
                    bank.getPersonList().get(bank.getPersonList().size() - 1).setAppendFromBank(plusWallet);
                } else if (isMoreOne(sumBank)) {
                        sumBank = sumBank.subtract(BigDecimal.ONE);
                        bank.getPersonList().get(bank.getPersonList().size() - 1).setAppendFromBank(BigDecimal.ONE);
                }
            }
        }

        BigDecimal res = BigDecimal.ZERO;
        for (Person person : bank.getPersonList()) {
            res = res.add(person.getWallet());
        }
    }

    public static BigDecimal alignmentWallet(List<Person> personList, BigDecimal sumBank) {
        int i = 1, j = 0;

        while (i < personList.size()) {
            Person current = personList.get(i);
            Person prev = personList.get(j);

            BigDecimal needSum = current.getSumWallet().subtract(prev.getSumWallet());
            if (j != i) {
                if (comparePrevLessCurrent(prev, current) && isNeedAmount(sumBank, needSum)) {
                    sumBank = sumBank.subtract(needSum);
                    prev.setAppendFromBank(needSum);
                } else if (comparePrevLessCurrent(prev, current)) {
                    prev.setAppendFromBank(sumBank);
                    sumBank = sumBank.subtract(sumBank);
                }
                j++;
            } else {
                j = 0;
                i++;
            }

        }

        return sumBank;
    }

    public static boolean isListAlign(List<Person> personList) {
        for (int i = 1; i < personList.size(); i++) {
            if (comparePrevLessCurrent(personList.get(i-1), personList.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNeedAmount(BigDecimal wallet, BigDecimal needSum) {
        return wallet.compareTo(needSum) > 0;
    }

    public static boolean isMoreZero(BigDecimal wallet) {
        return wallet.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isMoreOne(BigDecimal wallet) {
        return wallet.compareTo(BigDecimal.ONE) > 0;
    }

    public static boolean comparePrevLessCurrent(Person prev, Person curr) {
        BigDecimal p = prev.getSumWallet();
        BigDecimal c = curr.getSumWallet();
        return p.compareTo(c) < 0;
    }

    public static void writeResult(Bank bank) throws ParserConfigurationException, TransformerException {
        String root = "total";
        String result = "result";
        String person = "Person";
        String minimum = "minimum";

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        Element rootElement = document.createElement(root);
        document.appendChild(rootElement);

        Element elementResult = document.createElement(result);
        rootElement.appendChild(elementResult);

        for (Person p : bank.getPersonList()) {
            Element elPerson = document.createElement(person);
            elPerson.setAttribute("name", p.getName());
            elPerson.setAttribute("wallet", p.getWallet().toString());
            elPerson.setAttribute("appendFromBank", p.getAppendFromBank().toString());
            elementResult.appendChild(elPerson);
        }

        Element elMinimum = document.createElement(minimum);
        rootElement.appendChild(elMinimum);

        BigDecimal max = getMaxWallet(bank.getPersonList());

        for (Person p : bank.getPersonList()) {
            if (p.getSumWallet().setScale(0, RoundingMode.DOWN).compareTo(max) < 0) {
                Element elPerson = document.createElement(person);
                elPerson.setAttribute("name", p.getName());
                elMinimum.appendChild(elPerson);
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StreamResult res = new StreamResult(new StringWriter());

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
        transformer.transform(domSource, res);

        FileOutputStream fop = null;
        File file;
        try {
            file = new File(System.getProperty("user.dir")+"/output.xml");
            fop = new FileOutputStream(file);
            if (!file.exists()) {
                file.createNewFile();
            }
            String xmlResult = res.getWriter().toString();
            byte[] contentInBytes = xmlResult.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static BigDecimal getMaxWallet(List<Person> list) {
        BigDecimal max = BigDecimal.ZERO;
        for (Person person : list) {
            if (person.getSumWallet().compareTo(max) > 0) {
                max = person.getSumWallet();
            }
        }
        return max.setScale(0, RoundingMode.DOWN);
    }
}
