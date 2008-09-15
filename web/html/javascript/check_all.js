create_checkall_checkbox();

function blockEnter(evt) {
    evt = (evt) ? evt : event;
    var charCode = (evt.charCode) ? evt.charCode :((evt.which) ? evt.which : evt.keyCode);
    if (charCode == 13) {
        return false;
    } else {
        return true;
    }
}

function create_checkall_checkbox() {
  var checkall = document.getElementById("rhn_javascriptenabled_checkall_checkbox");

  if (checkall) {
    checkall.style.display = "inline";
  }
}

function check_all_on_page(form, set_label) {
  var form_name = form.name;
  if (form_name == "") {
    form_name = form.id;
  }
  var flag = eval("document.forms['" + form_name + "'].checkall.checked");
  var cboxes = eval("document.forms['" + form_name + "'].items_selected");
  process_check_all(set_label, cboxes, flag);
}

function process_check_all(set_label, cboxes, flag) {
  var i;
  var changed = new Array();

  if (cboxes.length) {
    for (i = 0; i < cboxes.length; i++) {
      //check the box only if it is enabled
      if (!cboxes[i].disabled) {
        if (cboxes[i].checked != flag) {
          changed.push(cboxes[i].value);
        } //if
        cboxes[i].checked = flag;
      } //if
    } //for
  } //if
  else {
    if (cboxes.checked != flag) {
      changed.push(cboxes.value)
    }
    cboxes.checked = flag;
  }

  update_server_set("ids", set_label, flag, changed);
}

function checkbox_clicked(thebox, set_label) {
  var form_name = thebox.form.name;
  if (form_name == "") {
    form_name = thebox.form.id;
  }
  var  checkall = eval("document.forms['" + form_name + "'].checkall");
  process_checkbox_clicked(thebox, set_label, checkall);
}

function process_checkbox_clicked(thebox, set_label, checkall) {
  var form_name = thebox.form.name;
  if (form_name == "") {
    form_name = thebox.form.id;
  }
  var i;
  var cboxes = eval("document.forms['" + form_name + "']." + thebox.name);

  var count_checked_or_disabled = 0;
  var all_checked = false;

  if (cboxes.length) {
    for (i = 0; i < cboxes.length; i++) {
      if (cboxes[i].checked || cboxes[i].disabled) {
        count_checked_or_disabled++;
      }
    }

    if (count_checked_or_disabled == cboxes.length) {
      all_checked = true;
    }
  }
  else {
    if (cboxes.checked) {
      all_checked = true;
    }
  }

  var a = new Array();
  a[0] = thebox.value;
  checkall.checked = all_checked;
  update_server_set("ids", set_label, thebox.checked, a);
}


function update_server_set(variable, set_label, checked, values) {
  
  var url = "/rhn/SetItemSelected.do";
  body = "set_label=" + set_label + "&";

  if (checked) {
    body = body + "checked=on";
  }
  else {
    body = body + "checked=off";
  }

  for (var i = 0; i < values.length; i++) {
    body =  body + "&" + variable + "=" + values[i];
  }
    
  if (set_label == "system_list") {
        new Ajax.Request(url, { method:'post',
                              postBody:body,
                              onSuccess:processSystemReqChange  }); 
  }
  else {
        new Ajax.Request(url, { method:'post',
                              postBody:body,
                              onSuccess:processPagination  }); 
  }
}

function processSystemReqChange(req, doc) {

      /*
       * Originally we were using getElementsByName, but the span
       * tag doesn't support a name attribute (according to XHTML standard)
       * this causes IE not to find them.  Works fine in Firefox.
       * So using the proper way method of getElementById.
       */
 

      // find SSM system selected element
      var hdr_selcnt = document.getElementById("header_selcount");

     
      // update the ssm header count (next to manage/clear buttons)
      var new_text = document.createTextNode(doc.header);
      hdr_selcnt.replaceChild(new_text, hdr_selcnt.firstChild);
     processPagination(req, doc);
}

function processPagination(req, doc) {
      // get the text we plan to show on top and bottom of listviews
      var pgcnt =
         document.createTextNode(doc.pagination);

      // update page selcount above and below listview
      // NOTE: couldn't get replaceChild to work, using nodeValue instead.
      var top = document.getElementById("pagination_selcount_top");
      top.firstChild.nodeValue = pgcnt.nodeValue;
      var bottom = document.getElementById("pagination_selcount_bottom");
      bottom.firstChild.nodeValue = pgcnt.nodeValue;
}
