(function() {
    var __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; },
        __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
        __hasProp = {}.hasOwnProperty;

    Annotator.Plugin.Motivations = (function(_super) {
        __extends(Motivations, _super);

        let collectionId = '';
        let actorSelected = false;
        let actionTargetSelected = false;
        let actionSelected = false;
        let characteristicSelected = false;
        let outcomeSelected = false;

        Motivations.prototype.options = {
            showField: true,
            motivations: [
                {
                    value: "oa:actor",
                    label: "Actor"
                },
                {
                    value: "oa:actionTarget",
                    label: "Action Target"
                },
                {
                    value: "oa:action",
                    label: "Action"
                },
                {
                    value: "oa:characteristic",
                    label: "Characteristic"
                },
                {
                    value: "oa:outcome",
                    label: "Outcome"
                }
            ]
        };

        Motivations.prototype.field = null;

        Motivations.prototype.input = null;

        Motivations.prototype.pluginInit = function() {
            var id, m, newfield, select, _i, _len, _ref;
            if (!Annotator.supported()) {
                return;
            }
            this.field = this.annotator.editor.addField({
                label: Annotator._t('ExplanatoryNote'),
                load: this.updateField,
                submit: this.setAnnotationMotivations
            });
            id = Annotator.$(this.field).find('input').attr('id');
            select = '<li class="annotator-item"><select style="width:100%">';
            _ref = this.options.motivations;
            for (_i = 0, _len = _ref.length; _i < _len; _i++) {
                m = _ref[_i];
                select += '<option value="' + m.value + '">' + m.label + '</option>';
            }
            select += '</select></li>';
            newfield = Annotator.$(select);
            Annotator.$(this.field).replaceWith(newfield);
            this.field = newfield[0];
            this.annotator.viewer.addField({
                load: this.updateViewer,
                annoPlugin: this
            });
            return this.input = Annotator.$(this.field).find('select');
        };

        function Motivations(element, options) {
            this.setAnnotationMotivations = __bind(this.setAnnotationMotivations, this);
            this.updateField = __bind(this.updateField, this);
            Motivations.__super__.constructor.apply(this, arguments);
            if (options.motivations) {
                this.options.motivations = options.motivations;
            }
        }

        Motivations.prototype.updateField = function(field, annotation) {
            var value;
            value = '';
            if (annotation.motivation) {
                value = annotation.motivation;
            }
            return this.input.val(value);
        };

        function uuid() {
            return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
                var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
                return v.toString(16);
            });
        }

        function escape(html) {
            return html
                .replace(/&(?!\w+;)/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
        }

        Motivations.prototype.setAnnotationMotivations = function(field, annotation) {
            if (collectionId) {
                console.log("collectionId set to " + collectionId);
            } else {
                collectionId = uuid();
                console.log("new collectionId set to " + collectionId);
            }
            annotation.tags = collectionId.split(",");
            if (this.input.val() == 'oa:actor') {
                actorSelected = true;
            }
            if (this.input.val() == 'oa:actionTarget') {
                actionTargetSelected = true;
            }
            if (this.input.val() == 'oa:outcome') {
                outcomeSelected = true;
            }
            if (this.input.val() == 'oa:characteristic') {
                characteristicSelected = true;
            }
            if (this.input.val() == 'oa:action') {
                actionSelected = true;
            }
            if (outcomeSelected && actionSelected && characteristicSelected && actionTargetSelected && actorSelected) {

                outcomeSelected = false;
                actionSelected = false;
                characteristicSelected = false;
                actionTargetSelected = false;
                actorSelected = false;
                collectionId = '';
            }
            return annotation.motivation = this.input.val();
        };

        Motivations.prototype.updateViewer = function(field, annotation) {
            var displayValue, m, _i, _len, _ref, _results;
            field = Annotator.$(field);
            if (annotation.motivation) {
                displayValue = annotation.motivation;
                _ref = this.annoPlugin.options.motivations;
                _results = [];
                for (_i = 0, _len = _ref.length; _i < _len; _i++) {
                    m = _ref[_i];
                    if (m.value === annotation.motivation) {
                        displayValue = m.label;
                        field.parent().parent().find('.annotator-motivation').html(escape(displayValue) + " ");
                        if (this.annoPlugin.options.showField) {
                            _results.push(field.addClass('annotator-motivation').html('<span class="annotator-motivation">' + escape(displayValue) + '</span>'));
                        } else {
                            _results.push(field.remove());
                        }
                    } else {
                        _results.push(void 0);
                    }
                }
                return _results;
            } else {
                return field.remove();
            }
        };

        return Motivations;

    })(Annotator.Plugin);

}).call(this);