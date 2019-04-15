package org.openmrs.module.idgen;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Before;

/**
 * test class for {@link SequentialIdentifierGenerator}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class SequentialIdentifierGeneratorTest {
	
	private IdentifierSourceService identifierSourceService;
	
	@Before
	public void setup() {
		mockStatic(Context.class);
		identifierSourceService = mock(IdentifierSourceService.class);
		
	}

	/**
	 * @verifies generate an identifier within minLength and maxLength bounds
	 * @see SequentialIdentifierGenerator#getIdentifierForSeed(long)
	 */
	@Test
	public void getIdentifierForSeed_shouldGenerateAnIdentifierWithinMinLengthAndMaxLengthBounds() throws Exception {
		SequentialIdentifierGenerator generator = new SequentialIdentifierGenerator();

		generator.setBaseCharacterSet("0123456789");
		generator.setPrefix("FOO-");
		generator.setSuffix("-ACK");
		generator.setFirstIdentifierBase("000");
		generator.setMinLength(11);
		generator.setMaxLength(13);

		assertThat(generator.getIdentifierForSeed(1), is("FOO-001-ACK"));
		assertThat(generator.getIdentifierForSeed(12), is("FOO-012-ACK"));
		assertThat(generator.getIdentifierForSeed(123), is("FOO-123-ACK"));
		assertThat(generator.getIdentifierForSeed(1234), is("FOO-1234-ACK"));
		assertThat(generator.getIdentifierForSeed(12345), is("FOO-12345-ACK"));
	}

	/**
	 * @verifies throw an error if generated identifier is shorter than minLength
	 * @see SequentialIdentifierGenerator#getIdentifierForSeed(long)
	 */
	@Test(expected = RuntimeException.class)
	public void getIdentifierForSeed_shouldThrowAnErrorIfGeneratedIdentifierIsShorterThanMinLength() throws Exception {
		SequentialIdentifierGenerator generator = new SequentialIdentifierGenerator();

		generator.setBaseCharacterSet("0123456789");
		generator.setPrefix("FOO-");
		generator.setMinLength(6);

		generator.getIdentifierForSeed(1);
	}

	/**
	 * @verifies throw an error if generated identifier is longer than maxLength
	 * @see SequentialIdentifierGenerator#getIdentifierForSeed(long)
	 */
	@Test(expected = RuntimeException.class)
	public void getIdentifierForSeed_shouldThrowAnErrorIfGeneratedIdentifierIsLongerThanMaxLength() throws Exception {
		SequentialIdentifierGenerator generator = new SequentialIdentifierGenerator();

		generator.setBaseCharacterSet("0123456789");
		generator.setPrefix("FOO-");
		generator.setMaxLength(1);

		generator.getIdentifierForSeed(1);
	}

	@Test
	public void shouldSetNextSequenceValueToNegative() throws Exception {
		SequentialIdentifierGenerator generator = new SequentialIdentifierGenerator();
		generator.setBaseCharacterSet("0123456789");
		generator.setPrefix("FOO-");
		generator.setSuffix("-ACK");
		generator.setFirstIdentifierBase("000");
		generator.setMinLength(11);
		generator.setMaxLength(13);
 		assertThat(generator.getNextSequenceValue(), is(-1l));
	}
	
	@Test
	public void getIdentifierForSeed_shouldGenerateLocationPrefixedIdFromUnInitializedSource() {
		SequentialIdentifierGenerator generator = new SequentialIdentifierGenerator();
		generator.setBaseCharacterSet("0123456789");
		generator.setFirstIdentifierBase("000");
		generator.setName("Location Prefixed Sequential Identifier Source");
		generator.setPrefixProviderBean("LocationBasedPrefixProvider");
		
		UserContext userContext = mock(UserContext.class);
		when(userContext.getLocation()).thenReturn(createLocationTree());
		when(Context.getUserContext()).thenReturn(userContext);
		when(Context.getService(IdentifierSourceService.class)).thenReturn(identifierSourceService);
		when(Context.getRegisteredComponent("LocationBasedPrefixProvider", PrefixProvider.class)).thenReturn(new LocationBasedPrefixProvider());
		
		assertThat(generator.getIdentifierForSeed(1L), is("AFDEL-000-001"));
	}
	
	private Location createLocationTree() {
		LocationAttributeType prefixAttrType = new LocationAttributeType();
		prefixAttrType.setName(IdgenConstants.PREFIX_LOCATION_ATTRIBUTE_TYPE);
		prefixAttrType.setMinOccurs(0);
		prefixAttrType.setMaxOccurs(1);
		prefixAttrType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		
		Location mainReg = new Location();
		mainReg.setName("Main Registration");
		
		Location kch = new Location();
		kch.setName("Kaboul Central Hospital");
		kch.addChildLocation(mainReg);
		
		LocationAttribute kchPrefixAtt = new LocationAttribute();
		kchPrefixAtt.setAttributeType(prefixAttrType);
		kchPrefixAtt.setValue("AFDEL-000-");
		kch.addAttribute(kchPrefixAtt);
		
		Location ks = new Location();
		ks.setName("Kaboul Subdelegation");
		ks.addChildLocation(kch);
		
		Location af = new Location();
		af.setName("Afghanistan Delegation");
		af.addChildLocation(ks);
		return mainReg;
	}
}
