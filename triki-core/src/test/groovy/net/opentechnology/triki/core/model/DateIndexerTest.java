/************************************************************************************
*
*   This file is part of triki
*
*   Written by Donald McIntosh (dbm@opentechnology.net) 
*
*   triki is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   triki is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with triki.  If not, see <http://www.gnu.org/licenses/>.
*
************************************************************************************/

package net.opentechnology.triki.core.model;

import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import net.opentechnology.triki.core.boot.TrikiBaseTest;

@RunWith(MockitoJUnitRunner.class)
public class DateIndexerTest extends TrikiBaseTest {

	private DateIndexer indexer;
	
	@Before
	public void setup() throws FileNotFoundException {
		loadModel();
		indexer = new DateIndexer();
		indexer.setSiteModel(model);
		when(config.getString("private_url")).thenReturn("http://www.donaldmcintosh.net");
	}
	
	@Test
	public void test() {
		indexer.index();
		printModel(model);
	}

}
