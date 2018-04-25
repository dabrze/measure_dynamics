/*
 *    SEAGenerator.java
 *    Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 *    @author Albert Bifet (abifet at cs dot waikato dot ac dot nz)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 *    
 */
package moa.streams.generators;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.DenseInstance;
import moa.core.FastVector;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;

import java.util.Random;
import moa.core.InstanceExample;

import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.core.ObjectRepository;
import moa.options.AbstractOptionHandler;
import com.github.javacliparser.FlagOption;
import com.github.javacliparser.IntOption;
import com.github.javacliparser.FloatOption;
import moa.streams.InstanceStream;
import moa.tasks.TaskMonitor;

/**
 * Stream generator for SEA concepts functions.
 * Generator described in the paper:<br/>
 * W. Nick Street and YongSeog Kim
 *    "A streaming ensemble algorithm (SEA) for large-scale classification",
 *     KDD '01: Proceedings of the seventh ACM SIGKDD international conference on Knowledge discovery and data mining
 *     377-382 2001.<br/><br/>
 *
 * Notes:<br/>
 * The built in functions are based on the paper.
 *
 * @author Albert Bifet (abifet at cs dot waikato dot ac dot nz)
 * @version $Revision: 7 $
 */
public class SEAGeneratorIm extends AbstractOptionHandler implements
        InstanceStream {

    @Override
    public String getPurposeString() {
        return "Generates SEA concepts functions.";
    }

    private static final long serialVersionUID = 1L;

    public IntOption functionOption = new IntOption("function", 'f',
            "Classification function used, as defined in the original paper.",
            1, 1, 4);

    public IntOption instanceRandomSeedOption = new IntOption(
            "instanceRandomSeed", 'i',
            "Seed for random generation of instances.", 1);

    public FlagOption balanceClassesOption = new FlagOption("balanceClasses",
            'b', "Balance the number of instances of each class.");
    
    public FlagOption class0MinorityOption = new FlagOption("class0Minority",
            'z', "If true, class 0 will be the minority one");

    public IntOption balanceRatioOption = new IntOption(
            "balanceRatio", 'r',
            "Ratio between the positive and negative class as 1:r, where r is the value of this parameter.", 1, 1, Integer.MAX_VALUE);
    
    public IntOption numInstancesConcept = new IntOption("numInstancesConcept", 'm',
            "The number of instances for each concept.", 0, 0, Integer.MAX_VALUE);

    public IntOption noisePercentageOption = new IntOption("noisePercentage",
            'n', "Percentage of noise to add to the data.", 10, 0, 100);

    public IntOption gradualRatioChangeStartOption = new IntOption(
            "gradualRatioChangeStart", 'g',
            "Change ratio gradually, starting from this instance, from 1:1 to 1:r, where r is the value of the balanceRatio parameter. If -1, the ratio does not change gradually.", -1, -1, Integer.MAX_VALUE);
    
    
    public IntOption gradualRatioChangeLengthOption = new IntOption(
            "gradualRatioChangeLength", 'l',
            "Change ratio gradually, during this number of instances, from 1:1 to 1:r, where r is the value of the balanceRatio parameter.", 1, 1, Integer.MAX_VALUE);
    
    
    
    protected interface ClassFunction {

        public int determineClass(double attrib1, double attrib2, double attrib3);
    }

    protected static ClassFunction[] classificationFunctions = {
        // function 1
        new ClassFunction() {

    @Override
    public int determineClass(double attrib1, double attrib2, double attrib3) {
        return (attrib1 + attrib2 <= 8) ? 0 : 1;
    }
},
        // function 2
        new ClassFunction() {

    @Override
    public int determineClass(double attrib1, double attrib2, double attrib3) {
        return (attrib1 + attrib2 <= 9) ? 0 : 1;
    }
},
        // function 3
        new ClassFunction() {

    public int determineClass(double attrib1, double attrib2, double attrib3) {
        return (attrib1 + attrib2 <= 7) ? 0 : 1;
    }
},
        // function 4
        new ClassFunction() {

    @Override
    public int determineClass(double attrib1, double attrib2, double attrib3) {
        return (attrib1 + attrib2 <= 9.5) ? 0 : 1;
    }
}
    };

    protected InstancesHeader streamHeader;

    protected Random instanceRandom;

    protected boolean nextClassShouldBeZero;
    
    protected int negativeInstanceCount = 0;

    public int numberInstance;
    
    @Override
    protected void prepareForUseImpl(TaskMonitor monitor,
            ObjectRepository repository) {
        // generate header
        FastVector attributes = new FastVector();
        attributes.addElement(new Attribute("attrib1"));
        attributes.addElement(new Attribute("attrib2"));
        attributes.addElement(new Attribute("attrib3"));

        FastVector classLabels = new FastVector();
        classLabels.addElement("groupA");
        classLabels.addElement("groupB");
        attributes.addElement(new Attribute("class", classLabels));
        this.streamHeader = new InstancesHeader(new Instances(
                getCLICreationString(InstanceStream.class), attributes, 0));
        this.streamHeader.setClassIndex(this.streamHeader.numAttributes() - 1);
        restart();
    }

    @Override
    public long estimatedRemainingInstances() {
        return -1;
    }

    @Override
    public InstancesHeader getHeader() {
        return this.streamHeader;
    }

    @Override
    public boolean hasMoreInstances() {
        return true;
    }

    @Override
    public boolean isRestartable() {
        return true;
    }

    @Override
    public InstanceExample nextInstance() {
    	numberInstance++;
        double attrib1 = 0, attrib2 = 0, attrib3 = 0;
        int group = 1;
        double balanceRatio = this.balanceRatioOption.getValue();
        
        if(this.gradualRatioChangeStartOption.getValue() != -1) {
        
        	if(numberInstance < this.gradualRatioChangeStartOption.getValue()) {
        		balanceRatio = 1.0;
        	} else if(numberInstance >= this.gradualRatioChangeStartOption.getValue()
	        	&& numberInstance < this.gradualRatioChangeStartOption.getValue() + this.gradualRatioChangeLengthOption.getValue()){
	        	balanceRatio = 1.0 + (balanceRatio/(1.0*this.gradualRatioChangeLengthOption.getValue())*(numberInstance-this.gradualRatioChangeStartOption.getValue()));
	        }
        }
        
        boolean desiredClassFound = false;
        while (!desiredClassFound) {
            // generate attributes
            attrib1 = 10 * this.instanceRandom.nextDouble();
            attrib2 = 10 * this.instanceRandom.nextDouble();
            attrib3 = 10 * this.instanceRandom.nextDouble();

            // determine class
            group = classificationFunctions[this.functionOption.getValue() - 1].determineClass(attrib1, attrib2, attrib3);
            
            if (!this.balanceClassesOption.isSet()) { 
        		double rand = this.instanceRandom.nextDouble();
        		
        		if(rand < (1.0/(balanceRatio+1.0))) {
        			if(group == 1) {
        				desiredClassFound = true;
        			}
        		} else {
        			if(group == 0) {
        				desiredClassFound = true;
        			}
        		}   
            } else {
                // balance the classes      
            	if (!this.nextClassShouldBeZero && group == 0 && this.negativeInstanceCount < balanceRatio - 1) {
            		this.negativeInstanceCount++;
            		desiredClassFound = true;
            	}
            	
                if ((!this.nextClassShouldBeZero && group == 0 && negativeInstanceCount >= balanceRatio - 1)
                        || (this.nextClassShouldBeZero && (group == 1))) {
                	this.negativeInstanceCount = 0;
                    desiredClassFound = true;
                    this.nextClassShouldBeZero = !this.nextClassShouldBeZero;
                } // else keep searching
            }
        }
        //Add Noise
        if ((1 + (this.instanceRandom.nextInt(100))) <= this.noisePercentageOption.getValue()) {
            group = (group == 0 ? 1 : 0);
        }

        // construct instance
        InstancesHeader header = getHeader();
        Instance inst = new DenseInstance(header.numAttributes());
        inst.setValue(0, attrib1);
        inst.setValue(1, attrib2);
        inst.setValue(2, attrib3);
        inst.setDataset(header);
        if (class0MinorityOption.isSet()) {
        	inst.setClassValue(group == 0 ? 1 : 0);
        } else {
        	inst.setClassValue(group);
        }
        return new InstanceExample(inst);
    }

    @Override
    public void restart() {
        this.instanceRandom = new Random(this.instanceRandomSeedOption.getValue());
        this.nextClassShouldBeZero = false;
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub
    }
}
