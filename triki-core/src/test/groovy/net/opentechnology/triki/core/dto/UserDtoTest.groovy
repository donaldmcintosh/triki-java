package net.opentechnology.triki.core.dto

import net.opentechnology.triki.core.boot.CachedPropertyStore
import net.opentechnology.triki.core.boot.TestUtils
import net.opentechnology.triki.schema.Dcterms
import net.opentechnology.triki.schema.Foaf
import net.opentechnology.triki.schema.Triki
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.StmtIterator
import spock.lang.Specification;

class UserDtoTest extends Specification {

    private Model model;
    private UserDto userDto = new UserDto();
    private GroupDto groupDto = new GroupDto();
    protected CachedPropertyStore cachedPropertyStore = Mock(CachedPropertyStore)

   def setup() throws FileNotFoundException {
       TestUtils testUtils = new TestUtils();
       model = testUtils.loadModel();
       userDto.setModel(model)
       groupDto.setModel(model)
       groupDto.setProps(cachedPropertyStore)
       userDto.setProps(cachedPropertyStore)
       userDto.setGroupDto(groupDto)
    }

    def addUser(){
        given:

        3 * cachedPropertyStore.getPrivateUrl() >> "http://localhost:8081/resource/"

        def details = [:]
        details.title = "Big Davie"
        details.name = "David Drysdale"
        details.login = "davie"
        details.password = "braw"
        details.email = "davie@goole.com"
        Resource friends = groupDto.addGroup("friends", "Friends")
        Resource personal = groupDto.getGroup("Personal")
        details.member = friends
        details.group = personal

        when:

        userDto.addUser("davie", details)

        then:

        Resource userRes = userDto.getUserByEmail("davie@goole.com")
        userRes.hasProperty(Foaf.mbox)
        userRes.hasProperty(Dcterms.title)
        userRes.hasProperty(Triki.restricted)
        userRes.hasProperty(Foaf.member)
    }

    def addAuthenticatedUser(){
        given:

        2 * cachedPropertyStore.getPrivateUrl() >> "http://localhost:8081/resource/"

        def name = "David Bowie"
        def email = "davie@mars.com"

        when:

        userDto.addAuthenticatedUser(name, email)

        then:

        Resource userRes = userDto.getUserByEmail("davie@mars.com")
        userRes.hasProperty(Foaf.mbox)
        userRes.hasProperty(Dcterms.title)
        userRes.hasProperty(Foaf.member)
    }

    def addUserToGroup(){
        given:

        4 * cachedPropertyStore.getPrivateUrl() >> "http://localhost:8081/resource/"

        def name = "David Bowie"
        def email = "davie@mars.com"

        when:

        groupDto.addGroup("hmrc", "hmrc")
        Resource user = userDto.addAuthenticatedUser(name, email)
        userDto.addUserToGroup(user, "hmrc")

        then:

        Resource userRes = userDto.getUserByEmail("davie@mars.com")
        userRes.hasProperty(Foaf.mbox)
        userRes.hasProperty(Dcterms.title)
        userRes.hasProperty(Foaf.member)
        StmtIterator groups = userRes.listProperties(Foaf.member)
        int i=0
        while(groups.hasNext()){
            groups.next()
            i++
        }
        i == 2
    }
}
