package net.opentechnology.triki.mtd.pages;

import java.time.LocalDate;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.opentechnology.triki.auth.components.FeedbackListContainer;
import net.opentechnology.triki.auth.components.FeedbackStringContainer;
import net.opentechnology.triki.auth.resources.SessionUtils;
import net.opentechnology.triki.mtd.enums.DateRange;
import net.opentechnology.triki.mtd.enums.RangeStartStop;
import net.opentechnology.triki.mtd.security.HmrcIdentityProvider;
import net.opentechnology.triki.mtd.validators.FormFieldNumericValidator;
import net.opentechnology.triki.mtd.validators.FormFieldRequiredValidator;
import net.opentechnology.triki.mtd.enums.VatObligationStatus;
import net.opentechnology.triki.mtd.vatapi.client.HmrcClientUtils;
import net.opentechnology.triki.mtd.vatapi.client.HmrcVatClient;
import net.opentechnology.triki.mtd.vatapi.dto.VatError;
import net.opentechnology.triki.mtd.vatapi.dto.VatObligation;
import net.opentechnology.triki.mtd.vatapi.dto.VatObligations;
import org.apache.log4j.Logger;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import retrofit2.Call;
import retrofit2.Response;

public class MtdVatObligations extends MtdVatManage {

  private static final Logger logger = Logger.getLogger(MtdVatObligations.class);

  @SpringBean
  private SessionUtils sessionUtils;

  @SpringBean
  private HmrcClientUtils hmrcVatClient;

  private List<VatObligation> obligationsResults;

  private ListView<VatObligation> results;

  public MtdVatObligations(PageParameters pageParameters) {

    super(pageParameters);
    add(new MtdVatObligationsForm("mtdVatObligationsForm", sessionUtils, hmrcVatClient));

    Fragment resultsPlaceholder = new  Fragment ("resultsSection", "resultsPlaceholder", this);
    add(resultsPlaceholder);

  }

  private class MtdVatObligationsForm extends Form<MtdVatObligationsForm> {

    private SessionUtils sessionUtils;
    private HmrcClientUtils hmrcClientUtils;
    private String vrn;
    private DateRange dateRange = DateRange.NEXT_6_MONTHS;
    private VatObligationStatus status = VatObligationStatus.ALL;
    private String hmrcHeaders;
    private FeedbackStringContainer obligationsFeedback;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MtdVatObligationsForm(String id, SessionUtils sessionUtils, HmrcClientUtils hmrcClientUtils) {
      super(id);
      this.sessionUtils = sessionUtils;
      this.hmrcClientUtils = hmrcClientUtils;

      setDefaultModel(new CompoundPropertyModel(this));

      TextField vrn = new TextField("vrn");
      FormFieldRequiredValidator vrnRequiredValidator = new FormFieldRequiredValidator("VRN");
      FormFieldNumericValidator vrnNumericValidator = new FormFieldNumericValidator("VRN");
      vrn.add(vrnRequiredValidator);
      vrn.add(vrnNumericValidator);
      add(vrn);
      FeedbackListContainer vrnFeedback = new FeedbackListContainer("vrnFeedback");
      vrnFeedback.setFilter(new ComponentFeedbackMessageFilter(vrn));
      add(vrnFeedback);

      DropDownChoice<DateRange> dateRangeChoice = new DropDownChoice<DateRange>("dateRange", new PropertyModel(this, "dateRange"),  Arrays.asList(DateRange.values()) );
      add(dateRangeChoice);
      FeedbackListContainer dateRangeFeedback = new FeedbackListContainer("dateRangeFeedback");
      dateRangeFeedback.setFilter(new ComponentFeedbackMessageFilter(dateRangeChoice));
      add(dateRangeFeedback);

      DropDownChoice<VatObligationStatus> statusChoice = new DropDownChoice<VatObligationStatus>("status", new PropertyModel(this, "status"), Arrays.asList(VatObligationStatus.values()));
      add(statusChoice);
      FeedbackListContainer statusFeedback = new FeedbackListContainer("statusFeedback");
      statusFeedback.setFilter(new ComponentFeedbackMessageFilter(statusChoice));
      add(statusFeedback);

      HiddenField hmrcHeaders = new HiddenField("hmrcHeaders");
      add(hmrcHeaders);

      obligationsFeedback = new FeedbackStringContainer("obligationsFeedback");
      add(obligationsFeedback);
    }


    @Override
    protected void onSubmit() {
      super.onSubmit();

      String accessToken = sessionUtils.getModuleToken(HmrcIdentityProvider.HMRC_TOKEN);
      try {
        HashMap<String, String> headers = objectMapper.readValue(hmrcHeaders, HashMap.class);
        headers.put("Gov-Client-Connection-Method", "WEB_APP_VIA_SERVER");

        RangeStartStop startStop = new RangeStartStop(dateRange);
        HmrcVatClient hmrcVatClient = hmrcClientUtils.buildAuthBearerServiceClient(accessToken);
        Call<VatObligations> callable = hmrcVatClient.getObligations(vrn,
            startStop.getStartFormatted(), startStop.getEndFormatted(),
                status.getCode(), headers, "");
        Response<VatObligations> vatObligationsResponse = callable.execute();

        if(vatObligationsResponse.code() == 200) {
          obligationsResults = vatObligationsResponse.body().getObligations();
          obligationsFeedback.setMsg(null);
        }
        else {
          VatError vatError = objectMapper. readValue(vatObligationsResponse.errorBody().bytes(), VatError.class);
          String key = "problems";
          if(vatError.getStatusCode() != null){
            key = vatError.getMessage();
          }
          else if(vatError.getCode() != null){
            key = vatError.getCode();
          }
          obligationsFeedback.setMsg(getResourceBundleMsg(key));
        }
      } catch (Exception e) {
        logger.error("Problems calling HMRC", e);
        obligationsFeedback.setMsg(getResourceBundleMsg("problems"));
      }
    }

    @Override
    protected void onConfigure() {
      super.onConfigure();
      setActiveMenu("obligations");
    }
  }

  @Override
  protected void onConfigure() {
    super.onConfigure();

    if(obligationsResults != null && obligationsResults.size() > 0){

      Fragment resultsSection = new  Fragment ("resultsSection", "resultsFragments", this);
      results = new ListView<VatObligation>("obligationResults", obligationsResults) {

        @Override
        protected void populateItem(ListItem<VatObligation> item) {
          VatObligation vatObligation = item.getModelObject();
          item.add(new Label("start", new PropertyModel(item.getModel(), "start")));
          item.add(new Label("end", new PropertyModel(item.getModel(), "end")));
          item.add(new Label("status", new PropertyModel(item.getModel(), "status")));
          item.add(new Label("due", new PropertyModel(item.getModel(), "due")));
          item.add(new Label("periodKey", new PropertyModel(item.getModel(), "periodKey")));
          item.add(new Label("received", new PropertyModel(item.getModel(), "received")));
          PageParameters pageParameters = new PageParameters();
          pageParameters.set("periodKey", vatObligation.getPeriodKey());
          BookmarkablePageLink viewReturnLink = new BookmarkablePageLink("viewReturn", MtdVatViewReturn.class, pageParameters);
          item.add(viewReturnLink);
          BookmarkablePageLink submitReturnLink = new BookmarkablePageLink("submitReturn", MtdVatSubmitReturn.class, pageParameters);
          item.add(submitReturnLink);

          if(vatObligation.getStatus() == VatObligationStatus.F){
            submitReturnLink.setVisible(false);
          }
          if(vatObligation.getStatus() == VatObligationStatus.O) {
            if (vatObligation.getDue().isBefore(LocalDate.now())) {
              viewReturnLink.setVisible(false);
            } else {
              submitReturnLink.setVisible(false);
              viewReturnLink.setVisible(false);
            }
          }
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
