package net.opentechnology.triki.mtd.pages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.opentechnology.triki.auth.components.FeedbackListContainer;
import net.opentechnology.triki.auth.components.FeedbackStringContainer;
import net.opentechnology.triki.auth.resources.SessionUtils;
import net.opentechnology.triki.mtd.enums.DateRange;
import net.opentechnology.triki.mtd.security.HmrcIdentityProvider;
import net.opentechnology.triki.mtd.validators.FormFieldNumericValidator;
import net.opentechnology.triki.mtd.validators.FormFieldRequiredValidator;
import net.opentechnology.triki.mtd.enums.VatObligationStatus;
import net.opentechnology.triki.mtd.vatapi.client.HmrcClientUtils;
import net.opentechnology.triki.mtd.vatapi.client.HmrcVatClient;
import net.opentechnology.triki.mtd.vatapi.dto.VatObligation;
import net.opentechnology.triki.mtd.vatapi.dto.VatObligations;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import retrofit2.Call;
import retrofit2.Response;

public class MtdVatObligations extends MtdVatManage {

  @SpringBean
  private SessionUtils sessionUtils;

  @SpringBean
  private HmrcClientUtils hmrcVatClient;

  public MtdVatObligations() {

    add(new MtdVatObligationsForm("mtdVatObligationsForm", sessionUtils, hmrcVatClient));

  }

  @Override
  protected void onConfigure() {
    super.onConfigure();
  }

  private class MtdVatObligationsForm extends Form<MtdVatObligationsForm> {

    private SessionUtils sessionUtils;
    private HmrcClientUtils hmrcClientUtils;
    private String vrn;
    private DateRange dateRange = DateRange.THIS_YEAR_AND_LAST;
    private VatObligationStatus status = VatObligationStatus.ALL;
    private String hmrcHeaders;
    private FeedbackStringContainer obligationsFeedback;
    private final List<String> statuses;
    private final List<String> dateRanges;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<VatObligation> obligationsResults;

    private ListView<VatObligation> results;

    public MtdVatObligationsForm(String id, SessionUtils sessionUtils, HmrcClientUtils hmrcClientUtils) {
      super(id);
      this.sessionUtils = sessionUtils;
      this.hmrcClientUtils = hmrcClientUtils;

      statuses = Arrays.stream(VatObligationStatus.values())
              .map(VatObligationStatus::getDescription)
              .collect(Collectors.toList());
      dateRanges = Arrays.stream(DateRange.values()).map(DateRange::getLabel).collect(Collectors.toList());

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

      DropDownChoice<String> dateRangeChoice = new DropDownChoice<String>("dateRange", new PropertyModel(this, "dateRange"), dateRanges);
      FormFieldRequiredValidator dateRangeRequiredValidator = new FormFieldRequiredValidator("Date range");
      dateRangeChoice.add(dateRangeRequiredValidator);
      add(dateRangeChoice);
      FeedbackListContainer dateRangeFeedback = new FeedbackListContainer("dateRangeFeedback");
      dateRangeFeedback.setFilter(new ComponentFeedbackMessageFilter(dateRangeChoice));
      add(dateRangeFeedback);

      DropDownChoice<String> statusChoice = new DropDownChoice<String>("status", new PropertyModel(this, "status"), statuses);
      FormFieldRequiredValidator statusRequiredValidator = new FormFieldRequiredValidator("Status");
      statusChoice.add(statusRequiredValidator);
      add(statusChoice);
      FeedbackListContainer statusFeedback = new FeedbackListContainer("statusFeedback");
      statusFeedback.setFilter(new ComponentFeedbackMessageFilter(statusChoice));
      add(statusFeedback);

      HiddenField hmrcHeaders = new HiddenField("hmrcHeaders");
      add(hmrcHeaders);

      obligationsFeedback = new FeedbackStringContainer("obligationsFeedback");
      add(obligationsFeedback);

      results = new ListView<VatObligation>("obligationResults", obligationsResults) {
        @Override
        protected void populateItem(ListItem<VatObligation> item) {
          item.add(new Label("start", new PropertyModel(item.getModel(), "start")));
          item.add(new Label("end", new PropertyModel(item.getModel(), "end")));
          item.add(new Label("status", new PropertyModel(item.getModel(), "status")));
        }
      };
      results.setReuseItems(true);
      add(results);
    }

    @Override
    protected void onSubmit() {
      super.onSubmit();

      String accessToken = sessionUtils.getModuleToken(HmrcIdentityProvider.HMRC_TOKEN);
      try {
        HashMap<String, String> headers = objectMapper.readValue(hmrcHeaders, HashMap.class);
        headers.put("Gov-Client-Connection-Method", "WEB_APP_VIA_SERVER");

        HmrcVatClient hmrcVatClient = hmrcClientUtils.buildAuthBearerServiceClient(accessToken);
        Call<VatObligations> callable = hmrcVatClient.getObligations(vrn,
            DateRange.format(dateRange.getStart()), DateRange.format(dateRange.getEnd()),
                status.getCode(), headers, "");
        Response<VatObligations> vatObligationsResponse = callable.execute();

        obligationsResults = vatObligationsResponse.body().getObligations();
        results.setList(obligationsResults);

      } catch (Exception e) {
        obligationsFeedback.setMsg("Problems calling HMRC");
      }
    }

  }
}
