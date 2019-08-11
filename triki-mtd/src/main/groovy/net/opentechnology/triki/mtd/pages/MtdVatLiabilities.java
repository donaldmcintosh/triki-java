package net.opentechnology.triki.mtd.pages;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.opentechnology.triki.auth.components.FeedbackListContainer;
import net.opentechnology.triki.auth.components.FeedbackStringContainer;
import net.opentechnology.triki.auth.resources.SessionUtils;
import net.opentechnology.triki.mtd.enums.DateRange;
import net.opentechnology.triki.mtd.enums.RangeStartStop;
import net.opentechnology.triki.mtd.security.HmrcIdentityProvider;
import net.opentechnology.triki.mtd.validators.FormFieldNumericValidator;
import net.opentechnology.triki.mtd.validators.FormFieldRequiredValidator;
import net.opentechnology.triki.mtd.vatapi.client.HmrcClientUtils;
import net.opentechnology.triki.mtd.vatapi.client.HmrcVatClient;
import net.opentechnology.triki.mtd.vatapi.dto.VatError;
import net.opentechnology.triki.mtd.vatapi.dto.VatLiability;
import net.opentechnology.triki.mtd.vatapi.dto.VatLiabilities;
import org.apache.log4j.Logger;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MtdVatLiabilities extends MtdVatManage {

  private static final Logger logger = Logger.getLogger(MtdVatLiabilities.class);

  @SpringBean
  private SessionUtils sessionUtils;

  @SpringBean
  private HmrcClientUtils hmrcVatClient;

  private List<VatLiability> liabilitiesResults;

  private ListView<VatLiability> results;

  public MtdVatLiabilities(PageParameters parameters) {
    super(parameters);
    add(new MtdVatLiabilities.MtdVatLiabilitiesForm("mtdVatLiabilitiesForm", sessionUtils, hmrcVatClient));

    Fragment resultsPlaceholder = new  Fragment ("resultsSection", "resultsPlaceholder", this);
    add(resultsPlaceholder);

  }

  private class MtdVatLiabilitiesForm extends Form<MtdVatLiabilities.MtdVatLiabilitiesForm> {

    private SessionUtils sessionUtils;
    private HmrcClientUtils hmrcClientUtils;
    private String vrn;
    private DateRange dateRange = DateRange.NEXT_6_MONTHS;
    private String hmrcHeaders;
    private FeedbackStringContainer liabilitiesFeedback;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MtdVatLiabilitiesForm(String id, SessionUtils sessionUtils, HmrcClientUtils hmrcClientUtils) {
      super(id);
      this.sessionUtils = sessionUtils;
      this.hmrcClientUtils = hmrcClientUtils;

      setDefaultModel(new CompoundPropertyModel(this));

      TextField vrnField = new TextField("vrn");
      FormFieldRequiredValidator vrnRequiredValidator = new FormFieldRequiredValidator("VRN");
      FormFieldNumericValidator vrnNumericValidator = new FormFieldNumericValidator("VRN");
      vrnField.add(vrnRequiredValidator);
      vrnField.add(vrnNumericValidator);
      add(vrnField);
      FeedbackListContainer vrnFeedback = new FeedbackListContainer("vrnFeedback");
      vrnFeedback.setFilter(new ComponentFeedbackMessageFilter(vrnField));
      add(vrnFeedback);

      DropDownChoice<DateRange> dateRangeChoice = new DropDownChoice<DateRange>("dateRange", new PropertyModel(this, "dateRange"),  Arrays.asList(DateRange.values()) );
      add(dateRangeChoice);
      FeedbackListContainer dateRangeFeedback = new FeedbackListContainer("dateRangeFeedback");
      dateRangeFeedback.setFilter(new ComponentFeedbackMessageFilter(dateRangeChoice));
      add(dateRangeFeedback);

      HiddenField hmrcHeaders = new HiddenField("hmrcHeaders");
      add(hmrcHeaders);

      liabilitiesFeedback = new FeedbackStringContainer("liabilitiesFeedback");
      add(liabilitiesFeedback);

      vrn = getVrn();
    }


    @Override
    protected void onSubmit() {
      super.onSubmit();

      String accessToken = sessionUtils.getModuleToken(HmrcIdentityProvider.HMRC_TOKEN);
      try {
        setVrn(vrn);
        HashMap<String, String> headers = objectMapper.readValue(hmrcHeaders, HashMap.class);
        setHmrcHeaders(headers);

        RangeStartStop startStop = new RangeStartStop(dateRange);
        HmrcVatClient hmrcVatClient = hmrcClientUtils.buildAuthBearerServiceClient(accessToken);
        Call<VatLiabilities> callable = hmrcVatClient.getLiabilities(vrn,
                startStop.getStartFormatted(), startStop.getEndFormatted(), headers, "");
        Response<VatLiabilities> vatLiabilitiesResponse = callable.execute();

        if(vatLiabilitiesResponse.code() == 200) {
          liabilitiesResults = vatLiabilitiesResponse.body().getLiabilities();
          liabilitiesFeedback.setMsg(null);
        }
        else {
          VatError vatError = objectMapper. readValue(vatLiabilitiesResponse.errorBody().bytes(), VatError.class);
          String key = "problems";
          if(vatError.getStatusCode() != null){
            key = vatError.getMessage();
          }
          else if(vatError.getCode() != null){
            key = vatError.getCode();
          }
          liabilitiesFeedback.setMsg(getResourceBundleMsg(key));
        }
      } catch (Exception e) {
        logger.error("Problems calling HMRC", e);
        liabilitiesFeedback.setMsg(getResourceBundleMsg("problems"));
      }
    }

    @Override
    protected void onConfigure() {
      super.onConfigure();
      setActiveMenu("liabilities");
    }
  }

  @Override
  protected void onConfigure() {
    super.onConfigure();

    if(liabilitiesResults != null && liabilitiesResults.size() > 0){

      Fragment resultsSection = new  Fragment ("resultsSection", "resultsFragments", this);
      results = new ListView<VatLiability>("liabilityResults", liabilitiesResults) {
        @Override
        protected void populateItem(ListItem<VatLiability> item) {
          item.add(new Label("fromDate", new PropertyModel(item.getModel(), "taxPeriod.from")));
          item.add(new Label("toDate", new PropertyModel(item.getModel(), "taxPeriod.to")));
          item.add(new Label("type", new PropertyModel(item.getModel(), "type")));
          item.add(new Label("originalAmount", new PropertyModel(item.getModel(), "originalAmount")));
          item.add(new Label("outstandingAmount", new PropertyModel(item.getModel(), "outstandingAmount")));
          item.add(new Label("due", new PropertyModel(item.getModel(), "due")));
        }
      };
      results.setReuseItems(true);
      resultsSection.add(results);


      replace(resultsSection);
    }
  }

  public String getResourceBundleMsg(String key){
    return getString(key);
  }
  
}
