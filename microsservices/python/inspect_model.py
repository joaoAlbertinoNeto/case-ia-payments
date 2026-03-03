import joblib

model = joblib.load(r'j:/case/case-ia-payments/ia/notebook/case.joblib')
print(type(model))
print(list(model.keys()))
print('feature_order=', model.get('feature_order'))
print('calibrator type', type(model.get('calibrator')))
